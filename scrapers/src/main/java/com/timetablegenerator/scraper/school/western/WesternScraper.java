package com.timetablegenerator.scraper.school.western;

import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.Period;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.scraper.annotation.LegacyConfig;
import com.timetablegenerator.scraper.annotation.LegacyMapping;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.annotation.SectionMapping;
import com.timetablegenerator.scraper.utility.ParsingTools;
import com.timetablegenerator.scraper.utility.network.RestRequest;
import com.timetablegenerator.scraper.utility.network.RestResponse;
import com.timetablegenerator.scraper.*;

import static com.timetablegenerator.scraper.annotation.LegacyMapping.LegacyType.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SchoolConfig(
        name = "University of Western Ontario", id = "western",
        sections = {

                @SectionMapping(code = "LEC", name = "Lecture"),
                @SectionMapping(code = "LAB", name = "Lab"),
                @SectionMapping(code = "TUT", name = "Tutorial"),
                @SectionMapping(code = "EXM", name = "Exam")
        },
        legacy = @LegacyConfig(
                year = 2018, term = TermClassifier.FULL_SCHOOL_YEAR,
                mapping = {
                        @LegacyMapping(from = "LEC", to = CORE),
                        @LegacyMapping(from = "LAB", to = LAB),
                        @LegacyMapping(from = "TUT", to = TUTORIAL),
                        @LegacyMapping(from = "EXM", to = UNUSED)
                }
        )
)
public class WesternScraper extends Scraper {

    private static final Logger LOGGER = LogManager.getLogger();

    private final String ORIGIN = "https://studentservices.uwo.ca";
    private final String TIMETABLE_ROOT = ORIGIN + "/secure/timetables/mastertt/ttindex.cfm";
    private final String EDUC_TIMETABLE_ROOT = ORIGIN + "/secure/timetables/eductt/ttindex.cfm";

    private final Pattern TIME_PATTERN =
            Pattern.compile("^(?<hour>[0-9]{1,2}):(?<minute>[0-9]{2}) (?<tod>[AP])M$");

    private final Pattern COURSE_PATTERN =
            Pattern.compile("^(?<code>(?<department>[A-Z-]+) [0-9]+(?<term>[A-Z]?)) - (?<name>.*)$");

    // Credit signifiers.
    private static final String QUARTER_CREDITS = "QRST";
    private static final String THREE_QUARTER_CREDITS = "K";
    private static final String HALF_CREDITS = "ABFGYZ";
    private static final String FULL_CREDITS = "EWXHJ";

    // Time fromName year signifiers.
    private static final String FIRST_TERM = "AFW";
    private static final String FIRST_TERM_FIRST_QUARTER = "Q";
    private static final String FIRST_TERM_SECOND_QUARTER = "R";

    private static final String SECOND_TERM = "BGX";
    private static final String SECOND_TERM_FIRST_QUARTER = "S";
    private static final String SECOND_TERM_SECOND_QUARTER = "T";

    private static final String FULL_YEAR_TERM = "EUHJK";

    private static final String UNSCHEDULED_TERM = "YZ";

    private static final int COOL_DOWN_PAUSE = 10;

    private Collection<Course> getCourses(Department department) throws IOException {

        // Create a blank request to acquire cookies.
        RestRequest request;

        if (!department.getCode().equals("EDUC")) {
            request = RestRequest.get(TIMETABLE_ROOT).run().nextPost(TIMETABLE_ROOT)
                    .setFormParameter("subject", department.getCode())
                    .setFormParameter("Designation", "Any")
                    .setFormParameter("catalognbr", "")
                    .setFormParameter("CourseTime", "All")
                    .setFormParameter("Component", "All")
                    .setFormParameter("time", "")
                    .setFormParameter("end_time", "")
                    .setFormParameter("day", "m")
                    .setFormParameter("day", "tu")
                    .setFormParameter("day", "w")
                    .setFormParameter("day", "th")
                    .setFormParameter("day", "f")
                    .setFormParameter("Campus", "Any")
                    .setFormParameter("command", "search");
        } else {
            // The EDUC department requires special handling.
            request = RestRequest.get(EDUC_TIMETABLE_ROOT).run().nextPost(EDUC_TIMETABLE_ROOT)
                    .setHeader("Referer", EDUC_TIMETABLE_ROOT)
                    .setHeader("Origin", ORIGIN)
                    .setFormParameter("subject", "EDUC")
                    .setFormParameter("catalognbr", "")
                    .setFormParameter("command", "search");
        }

        Document d;

        int i = COOL_DOWN_PAUSE;

        while (true) {

            d = Jsoup.parse(request.run().getResponseString());

            if (d.select(".g-recaptcha").size() > 0)
                LOGGER.warn(" >>> CAPTCHA detected, pausing for cool-down... [" + i + "s]");
            else
                break;

            try {

                Thread.sleep(1000 * i);
                i += 5;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Collection<Course> courses = new ArrayList<>();

        String nextCaption = null, nextDescription = null;

        // Iterate through the course listings.
        for(Element e: d.select(".table-striped,h4,p.font-md")) {
            switch(e.tag().getName().toLowerCase()){
                case "h4": nextCaption = e.text(); break;
                case "p": nextDescription = e.ownText(); break;
                case "table": {
                    Course c = parseCourseData(nextCaption, nextDescription, e, department);
                    courses.add(c);
                } break;
            }
        }

        return courses;
    }

    private Course parseCourseData(String caption, String description, Element courseElement, Department department) {

        final Course c;

        if (department.getCode().equals("EDUC")) {
            // EDUC courses are strangely formatted and required special handling.
            caption = "EDUC ";
            Set<String> codes = courseElement.select("table.table-striped > tbody > tr > td:first-child")
                    .stream().map(Element::text).collect(Collectors.toSet());
            if (codes.size() > 1){
                throw new IllegalStateException("Course contains more than 1 code: " + codes.toString());
            }
            caption += codes.iterator().next() + " - ";
        }

        Matcher courseMatch = COURSE_PATTERN.matcher(caption);

        if (!courseMatch.find()) {
            throw new IllegalArgumentException("Could not parse the course designation \"" + caption + "\".");
        }

        if (!courseMatch.group("department").equals(department.getCode())) {
            throw new IllegalArgumentException("The department signifier to this course deviates from the expected ("
                    + department.getCode() + ").");
        }

        String courseCode = courseMatch.group("code");
        String courseName = courseMatch.group("name");

        String termId = courseMatch.group("term");

        if (termId.length() == 0 || FULL_YEAR_TERM.contains(termId)) {
            c = new Course(getSchool(), TermClassifier.FULL_SCHOOL_YEAR, department,
                    courseCode, courseName, 1.00);
            c.setDescription(description);
        } else {

            TermClassifier courseTerm;

            // Find the term the course belongs to.
            if (FIRST_TERM.contains(termId))
                courseTerm = TermClassifier.FALL;
            else if (FIRST_TERM_FIRST_QUARTER.contains(termId))
                courseTerm = TermClassifier.FALL_FIRST_QUARTER;
            else if (FIRST_TERM_SECOND_QUARTER.contains(termId))
                courseTerm = TermClassifier.FALL_SECOND_QUARTER;
            else if (SECOND_TERM.contains(termId))
                courseTerm = TermClassifier.SPRING;
            else if (SECOND_TERM_FIRST_QUARTER.contains(termId))
                courseTerm = TermClassifier.SPRING_FIRST_QUARTER;
            else if (SECOND_TERM_SECOND_QUARTER.contains(termId))
                courseTerm = TermClassifier.SPRING_SECOND_QUARTER;
            else if (UNSCHEDULED_TERM.contains(termId))
                courseTerm = TermClassifier.UNSCHEDULED;
            else
                throw new IllegalArgumentException("Unknown term signifier \"" + termId + "\"");

            double credits;

            // Find the credit value fromName the course.
            if (QUARTER_CREDITS.contains(termId))
                credits = 0.25;
            else if (HALF_CREDITS.contains(termId))
                credits = 0.50;
            else if (THREE_QUARTER_CREDITS.contains(termId))
                credits = 0.75;
            else if (FULL_CREDITS.contains(termId))
                credits = 1.00;
            else
                throw new IllegalArgumentException("Unknown credit signifier \"" + courseTerm + "\"");

            c = new Course(getSchool(), courseTerm, department, courseCode, courseName, credits);
        }

        // Start analyzing course data.
        Elements sectionList = courseElement.select("table.table-striped > tbody > tr");

        for (Element sectionElement : sectionList) {

            ListIterator<Element> columns = sectionElement.select("td").listIterator();

            // The first column of education courses is always the course number, which is dropped.
            if (department.getCode().equals("EDUC")) {
                columns.next();
            }

            String sectionId = ParsingTools.sanitize(columns.next().ownText());
            String sectionType = ParsingTools.sanitize(columns.next().ownText());
            String sectionSerial = ParsingTools.sanitize(columns.next().ownText());

            Section section;

            if (c.getSectionType(sectionType) == null || c.getSectionType(sectionType).getSection(sectionId) == null) {

                section = Section.fromName(sectionId).setSerialNumber(sectionSerial);
                c.addSection(sectionType, section);
            } else
                section = c.getSectionType(sectionType).getSection(sectionId);

            columns.next();

            boolean[] dayScheduled = new boolean[6];

            // Iterate only from Monday to Friday. Western schedules no courses on the weekend.
            for (DayOfWeek dow : DayOfWeek.values()) {

                if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
                    continue;

                dayScheduled[dow.getValue()] = !ParsingTools.sanitize(columns.next().text()).equals("");
                dayScheduled[0] |= dayScheduled[dow.getValue()];
            }

            String startTimeString = ParsingTools.sanitize(columns.next().text());
            String endTimeString = ParsingTools.sanitize(columns.next().text());

            // Get the time period room.
            String location = ParsingTools.sanitize(columns.next().ownText());

            if (location.isEmpty())
                location = null;

            // Get the instructors for this particular time period.
            List<String> supervisors = new ArrayList<>();

            for (TextNode supervisor : columns.next().textNodes()) {

                String supervisorText = ParsingTools.sanitize(supervisor.text());

                if (!supervisorText.equals(""))
                    supervisors.add(supervisorText);
            }

            boolean onlineDetected = ParsingTools.sanitize(columns.next().ownText())
                    .toLowerCase().contains("online");

            // If this section has not already been parsed, make a new one.
            if (section == null) {

                section = Section.fromName(sectionId)
                        .setSerialNumber(sectionSerial)
                        .setOnline(onlineDetected);

                c.addSection(sectionType, section);
            }

            String sectionFull = ParsingTools.sanitize(columns.next().text());

            boolean isFull;

            switch (sectionFull) {
                case "Not Full":
                    isFull = false;
                    break;
                case "Full":
                    isFull = true;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown fullness specifier \"" + sectionFull + "\"");
            }

            Matcher startTimeMatch = TIME_PATTERN.matcher(startTimeString);
            Matcher endTimeMatch = TIME_PATTERN.matcher(endTimeString);

            List<Period> rps = new ArrayList<>();

            if (startTimeMatch.find() && endTimeMatch.find()) {

                int[] startTime = {Integer.parseInt(startTimeMatch.group("hour")),
                        Integer.parseInt(startTimeMatch.group("minute"))};

                int[] endTime = {Integer.parseInt(endTimeMatch.group("hour")),
                        Integer.parseInt(endTimeMatch.group("minute"))};

                if (startTimeMatch.group("tod").charAt(0) == 'P')
                    startTime[0] += (startTime[0] != 12 ? 12 : 0);

                if (endTimeMatch.group("tod").charAt(0) == 'P')
                    endTime[0] += (endTime[0] != 12 ? 12 : 0);

                LocalTime startTimeData = LocalTime.of(startTime[0], startTime[1]);
                LocalTime endTimeData = LocalTime.of(endTime[0], endTime[1]);

                if (!dayScheduled[0]) {

                    section.addPeriod(new RepeatingPeriod(c.getTerm()).setRoom(location));

                } else
                    for (DayOfWeek dow : DayOfWeek.values())
                        if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY)
                            if (dayScheduled[dow.getValue()]) {
                                rps.add(new RepeatingPeriod(c.getTerm())
                                        .setTime(dow, startTimeData, endTimeData).setRoom(location));
                            }
            } else {

                if (startTimeString.isEmpty() && endTimeString.isEmpty()) {

                    if (!dayScheduled[0]) {
                        rps.add(new RepeatingPeriod(c.getTerm()).setRoom(location));

                    } else
                        for (DayOfWeek dow : DayOfWeek.values())
                            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY)
                                if (dayScheduled[dow.getValue()]) {
                                    rps.add(new RepeatingPeriod(c.getTerm()).setRoom(location));
                                }
                } else
                    throw new IllegalArgumentException(
                            String.format("Failure parsing time strings: start [%s] and/or end [%s]",
                                    startTimeString, endTimeString));
            }

            for (Period p : rps) {
                p.addSupervisors(supervisors);
                section.addPeriod(p);
            }

            section.setFull(isFull);
        }

        return c;
    }

    @Override
    public TimeTable retrieveTimetable(Term term) throws IOException {

        TimeTable tt = new TimeTable(getSchool(), term);

        // Western doesn't use keys. This should be closely monitored in the event that changes.
        RestResponse response = RestRequest.get(TIMETABLE_ROOT).run();

        // Get the list to departments.
        Elements rows = Jsoup.parse(response.getResponseString()).select("#inputSubject > option");

        Collection<Department> departments = new ArrayList<>();

        for (Element departmentBlock : rows) {

            String name = ParsingTools.sanitize(departmentBlock.ownText());
            String code = ParsingTools.sanitize(departmentBlock.attr("value"));

            if (name.equals("All Subjects")) {
                continue;
            }

            departments.add(new Department(code, name));
        }

        // Add the education department.
        departments.add(new Department("EDUC", "Education"));

        int i = 0;

        for (Department department : departments) {

            LOGGER.info("Retrieving data under \"" + department.getName()
                    + "\" (" + department.getCode() + ") [" + ++i + " of " + departments.size() + "]...");

            Collection<Course> courses = getCourses(department);
            courses.forEach(tt::addCourse);

            if (courses.isEmpty())
                LOGGER.info("-> No courses discovered");
            else
                LOGGER.info("-> " + courses.size() + " courses discovered");
        }

        return tt;
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {

        RestResponse resp = RestRequest.get(TIMETABLE_ROOT).run();
        Document d = Jsoup.parse(resp.getResponseString());

        // retry in case of captcha

        int i = 0;
        while (d.select(".g-recaptcha").size() > 0) {
            int pause = COOL_DOWN_PAUSE + 5 * i++;
            LOGGER.warn(" >>> CAPTCHA detected, pausing for cool-down... [" + pause + "s]");
            try {
                Thread.sleep(1000 * pause);
            } catch (InterruptedException ie) {
                System.out.println("Interrupted prematurely?");
            }
            resp = RestRequest.get(TIMETABLE_ROOT).run();
            d = Jsoup.parse(resp.getResponseString());
        }

        Element e = d.select("small.resizeSmall").first();

        if (e == null) {
            throw new IllegalArgumentException("Cannot locate header string. Perhaps the web page layout has changed?");
        }

        String value = ParsingTools.sanitize(e.ownText());

        Set<Term> returnSet = new HashSet<>();
        returnSet.add(Term.findProbableTerm(value, ""));

        return returnSet;
    }
}
