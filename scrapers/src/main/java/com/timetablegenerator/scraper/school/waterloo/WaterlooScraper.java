package com.timetablegenerator.scraper.school.waterloo;

import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.scraper.utility.ParsingTools;
import com.timetablegenerator.scraper.utility.network.RestRequest;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.Scraper;
import com.timetablegenerator.scraper.annotation.SectionMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SchoolConfig(
        name = "University of Waterloo", id = "waterloo",
        sections = {

                @SectionMapping(code = "CLN", name = "Clinic"),
                @SectionMapping(code = "DIS", name = "Discussion"),
                @SectionMapping(code = "ENS", name = "Ensemble"),
                @SectionMapping(code = "ESS", name = "Essay"),
                @SectionMapping(code = "FLD", name = "Field Study"),
                @SectionMapping(code = "FLT", name = "Flight"),
                @SectionMapping(code = "LAB", name = "Laboratory"),
                @SectionMapping(code = "LEC", name = "Lecture"),
                @SectionMapping(code = "ORL", name = "Oral Conversation"),
                @SectionMapping(code = "OLN", name = "Online"),
                @SectionMapping(code = "PRA", name = "Practicum"),
                @SectionMapping(code = "PRJ", name = "Project"),
                @SectionMapping(code = "RDG", name = "Reading"),
                @SectionMapping(code = "SEM", name = "Seminar"),
                @SectionMapping(code = "STU", name = "Studio"),
                @SectionMapping(code = "TLC", name = "Test/Lecture"),
                @SectionMapping(code = "TUT", name = "Tutorial"),
                @SectionMapping(code = "WRK", name = "Work Term"),
                @SectionMapping(code = "WSP", name = "Workshop"),
                @SectionMapping(code = "TST", name = "Test")
        }
)
public class WaterlooScraper extends Scraper {

    private final Logger LOGGER = LogManager.getLogger();

    private final static String TIMETABLE_URL = "http://www.adm.uwaterloo.ca/infocour/CIR/SA/under.html";
    private final static String COURSE_URL = "http://www.adm.uwaterloo.ca/cgi-bin/cgiwrap/infocour/salook.pl";

    private final static String DEPARTMENTS_URL = "http://ugradcalendar.uwaterloo.ca/page/Course-Descriptions-Index";

    private final Pattern TERM_FINDER =
            Pattern.compile("What term are you looking for\\? \\((?<terms>.+)\\)");

    private final Pattern TIME_OF_DAY_PATTERN =
            Pattern.compile("^(?<shour>[0-9]{2}):(?<sminute>[0-9]{2})-(?<ehour>[0-9]{2}):(?<eminute>[0-9]{2})(?<days>[MThWFSuU]+)\\s?(?<dates>.*)$");

    private final Pattern DATE_PATTERN =
            Pattern.compile("(?<startmonth>[0-9]{1,2})/(?<startday>[0-9]{1,2})-(?<endmonth>[0-9]{1,2})/(?<endday>[0-9]{1,2})");

    private Collection<Department> getDepartments() throws IOException {

        Collection<Department> departments = new ArrayList<>();

        Document d = Jsoup.parse(RestRequest.get(DEPARTMENTS_URL).run().getResponseString());

        Elements rows = d.select("a[name=A]").first().parent().parent().parent().parent().select("tr");

        for (Element row : rows){

            Element rowCell = row.select("td").first();
            String departmentName = ParsingTools.sanitize(rowCell.ownText());
            String departmentCode = ParsingTools.sanitize(rowCell.nextElementSibling().ownText());

            if (departmentCode.isEmpty())
                continue;

            departments.add(new Department(departmentCode, departmentName));
        }

        return departments;
    }

    @Override
    public TimeTable retrieveTimetable(Term term) throws IOException {

        TimeTable tt = new TimeTable(getSchool(), term);

        Collection<Department> departments = getDepartments();

        int i = 0;

        // Only check departments discovered. The others unlisted are typically empty or unimportant.
        for (Department department : departments) {

            LOGGER.info("Reading information for department " + department.getName() + " [" + department.getCode()
                    + "] (" + ++i + " of " + departments.size() +") ...");

            Document dsd = Jsoup.parse(
                    RestRequest.post(COURSE_URL)
                            .setFormParameter("level", "under")
                            .setFormParameter("sess", term.getKey())
                            .setFormParameter("subject", department.getCode())
                            .setFormParameter("cournum", "").run().getResponseString()
            );

            Elements courseRows = dsd.select("table");

            if (courseRows.isEmpty()) {

                LOGGER.info("\t-> No results");
                continue;
            }

            courseRows = courseRows.first().select("tbody").first().children();

            Course c = null;

            int numberOfCourses = 0;

            for (Element courseRow : courseRows) {

                // Ignore the header rows.
                if (courseRow.children().first().tagName().equals("th"))
                    continue;

                Iterator<Element> columnCursor = courseRow.children().iterator();

                String headerText = ParsingTools.sanitize(columnCursor.next().text());

                // If the header is the ID fromName the department, it is a descriptor for a course.
                if (headerText.equals(department.getCode())) {

                    String courseCode = ParsingTools.sanitize(columnCursor.next().ownText());
                    double credits = Double.parseDouble(ParsingTools.sanitize(columnCursor.next().ownText()));
                    String courseName = ParsingTools.sanitize(columnCursor.next().ownText());

                    c = new Course(getSchool(), term.getTermId(), department, courseCode, courseName, credits);

                    continue;
                }

                if (headerText.isEmpty()) {

                    // Skip if we're in between courses.
                    if (c == null)
                        continue;

                    populateCourse(term, c, columnCursor.next());

                    numberOfCourses++;
                    tt.addCourse(c);

                    c = null;
                }
            }

            LOGGER.info("\t-> Parsed " + numberOfCourses + " courses.");
        }

        return tt;
    }

    private void populateCourse(Term t, Course c, Element courseElement) {

        Iterator<Element> rows = courseElement.select("tr").iterator();

        // Toss the header row.
        rows.next();

        while (rows.hasNext()) {

            Iterator<Element> columnCursor = rows.next().select("td").iterator();

            String serial = ParsingTools.sanitize(columnCursor.next().text());

            if (!serial.matches("[0-9]+"))
                continue;

            String[] sectionInfo = ParsingTools.sanitize(columnCursor.next().text()).split(" ");

            String onlineText = columnCursor.next().text();

            // Let's save the parser some trouble and jump over online courses.
            boolean online = onlineText.contains("ONLINE")
                    | onlineText.contains("OLN");

            Section s = Section.fromName(sectionInfo[1])
                    .setSerialNumber(serial)
                    .setOnline(online);

            c.addSection(sectionInfo[0], s);

            columnCursor.next();
            columnCursor.next();
            columnCursor.next();

            int maxEnrollment = Integer.parseInt(ParsingTools.sanitize(columnCursor.next().text()));
            int enrollment = Integer.parseInt(ParsingTools.sanitize(columnCursor.next().text()));

            s.setEnrollment(enrollment).setMaximumEnrollment(maxEnrollment);

            int maxWaiting = Integer.parseInt(ParsingTools.sanitize(columnCursor.next().text()));
            int waiting = Integer.parseInt(ParsingTools.sanitize(columnCursor.next().text()));

            if (maxWaiting > 0)
                s.setWaiting(waiting).setMaximumWaiting(maxWaiting);

            // Parse the time data.
            String timeString = ParsingTools.sanitize(columnCursor.next().text());

            String location = ParsingTools.sanitize(columnCursor.next().text());

            // Get rid fromName null locations.
            if (location.isEmpty())
                location = null;

            List<String> supervisors = new ArrayList<>();

            // The instructors text node will not exist if no instructors are assigned.
            if (columnCursor.hasNext())
                for (TextNode instructor : columnCursor.next().textNodes()) {

                    // Let's present the names more nicely.
                    String[] name = ParsingTools.sanitize(instructor.text()).split(",");
                    supervisors.add(String.format("%s %s", name[1], name[0]));
                }

            if (timeString.equals("TBA")) {

                s.addPeriod(new RepeatingPeriod(t.getTermId())
                        .setRoom(location).addSupervisors(supervisors));

            } else {

                Matcher timeMatch = TIME_OF_DAY_PATTERN.matcher(timeString);

                if (!timeMatch.find())
                    throw new IllegalArgumentException("Failed to parse time from string \"" + timeString + "\"");

                LocalTime startTime = LocalTime.of(Integer.parseInt(timeMatch.group("shour")),
                        Integer.parseInt(timeMatch.group("sminute")));

                LocalTime endTime = LocalTime.of(Integer.parseInt(timeMatch.group("ehour")),
                        Integer.parseInt(timeMatch.group("eminute")));

                String dateData = timeMatch.group("dates");

                if (endTime.isBefore(startTime))
                    endTime = endTime.plusHours(12);

                if (startTime.getHour() < 8) {

                    startTime = startTime.plusHours(12);
                    endTime = endTime.plusHours(12);
                }

                // Thursdays and Sundays are double character; let's change their representation from 'Th' to 'R'
                // and 'Su' to 'N' respectively.
                String days = timeMatch.group("days").replace("Th", "R").replace("Su", "N");

                for (char day : days.toCharArray()) {

                    DayOfWeek d = null;

                    switch (day) {

                        case 'M':
                            d = DayOfWeek.MONDAY;
                            break;
                        case 'T':
                            d = DayOfWeek.TUESDAY;
                            break;
                        case 'W':
                            d = DayOfWeek.WEDNESDAY;
                            break;
                        case 'R':
                            d = DayOfWeek.THURSDAY;
                            break;
                        case 'F':
                            d = DayOfWeek.FRIDAY;
                            break;
                        case 'S':
                            d = DayOfWeek.SATURDAY;
                            break;
                        case 'N':
                            d = DayOfWeek.SUNDAY;
                            break;
                        case 'U': // This may signify an unknown day.
                            LOGGER.warn("Witnessed the strange day-of-week signifier \"U\"");
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown day of week signifier \"" + day + "\"");
                    }

                    if (d == null)
                        continue;

                    // If there is no date, then it is a repeating time period.
                    if (dateData.isEmpty()) {

                        s.addPeriod(new RepeatingPeriod(t.getTermId())
                                .setTime(d, startTime, endTime).setRoom(location)
                                .addSupervisors(supervisors));
                    } else {

                        Matcher dateMatch = DATE_PATTERN.matcher(dateData);

                        if (!dateMatch.find())
                            throw new IllegalArgumentException("Unable to parse timing information \""
                                    + dateData + "\"");

                        LocalDateTime startDateTime = LocalDateTime.of(t.getYear(),
                                Integer.parseInt(dateMatch.group("startmonth")),
                                Integer.parseInt(dateMatch.group("startday")), 0, 0
                        );

                        LocalDateTime endDateTime = LocalDateTime.of(t.getYear(),
                                Integer.parseInt(dateMatch.group("endmonth")),
                                Integer.parseInt(dateMatch.group("endday")), 0, 0
                        );

                        s.addPeriod(new OneTimePeriod(t.getTermId())
                                .setDateTimes(startDateTime, endDateTime)
                                .setRoom(location).addSupervisors(supervisors));
                    }
                }
            }
        }
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {

        Document d = Jsoup.parse(RestRequest.get(TIMETABLE_URL).run().getResponseString());

        // Waterloo calls the summer term "Spring". Spring is replaced with summer to not
        // confuse the parsing tool.
        String headerText = ParsingTools.sanitize(
                d.select("form[action=/cgi-bin/cgiwrap/infocour/salook.pl]")
                        .first().ownText()).replaceAll("[sS]pring", "summer");

        Matcher m = TERM_FINDER.matcher(headerText);

        if (m.find()) {

            String terms = m.group("terms");

            Set<Term> resultSet = new HashSet<>();

            for (String term : terms.split(",")) {

                String[] termContent = term.split("=");

                if (termContent.length != 2)
                    throw new IllegalArgumentException("");

                resultSet.add(Term.findProbableTerm(termContent[1], termContent[0]));
            }

            return resultSet;

        } else
            throw new IllegalArgumentException("");

    }
}