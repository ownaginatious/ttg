package com.timetablegenerator.scraper.school.uoft;

import static com.timetablegenerator.scraper.annotation.LegacyMapping.LegacyType.*;

import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.scraper.annotation.LegacyConfig;
import com.timetablegenerator.scraper.annotation.LegacyMapping;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.annotation.SectionMapping;
import com.timetablegenerator.scraper.utility.ParsingTools;
import com.timetablegenerator.scraper.utility.network.RestRequest;
import com.timetablegenerator.scraper.utility.network.RestResponse;
import com.timetablegenerator.scraper.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SchoolConfig(
        name = "University of Toronto Scarborough", id = "utsc",
        sections = {
                @SectionMapping(name = "Lecture", code = "LEC"),
                @SectionMapping(name = "Practical", code = "PRA"),
                @SectionMapping(name = "Tutorial", code = "TUT")
        },
        legacy = @LegacyConfig(
                year = 2018, term = TermClassifier.FULL_SCHOOL_YEAR,
                mapping = {
                        @LegacyMapping(from = "LEC", to = CORE),
                        @LegacyMapping(from = "PRA", to = LAB),
                        @LegacyMapping(from = "TUT", to = TUTORIAL)
                }
        )
)
public class UofTScarboroughScraper extends Scraper {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern COURSE_CODE_DEP_MATCHER =
            Pattern.compile("(?<code>(?<department>[A-Z]{3})[A-Z0-9]{3,4}(?<credits>[HY])[0-9]?(?<term>[FSY])) -\\s?(?<name>.*)");

    private static final String ROOT_URL = "https://www.utsc.utoronto.ca/";
    private static final String TIMETABLE_URL = ROOT_URL + "regoffice/timetable/timetable.php";

    private Collection<Department> getDepartments() throws IOException {

        Document d = Jsoup.parse(RestRequest.get(TIMETABLE_URL).run().getResponseString());

        Collection<Department> departmentsMap = new ArrayList<>();

        for (Element department : d.getElementById("dept").children()) {

            String code = ParsingTools.sanitize(department.attr("value"));
            String name = ParsingTools.sanitize(department.ownText());

            if (!code.equals("DISPLAY_ALL"))
                departmentsMap.add(new Department(code, name));
        }
        return departmentsMap;
    }

    @Override
    public TimeTable retrieveTimetable(Term term) throws IOException {

        LOGGER.info("Initiating extraction for U of T Scarborough term: " + term.toString() + "...");

        TimeTable tt = new TimeTable(getSchool(), term);

        // Get the
        int i = 0;

        LOGGER.info("Extracting departments...");

        Collection<Department> departments = this.getDepartments();

        // Iterate over the departments.
        for (Department department : departments) {

            LOGGER.info("Retrieving data for department ["
                    + department.getCode() + " -> "
                    + department.getName() + "] {" + ++i + "/" + departments.size() + "}...");

            Collection<Course> courses = retrieveCourses(term, department);

            if (courses.isEmpty())
                LOGGER.info("No course data discovered for this department.");
            else {

                LOGGER.info("Discovered " + courses.size() + " courses.");

                courses.forEach(c -> {
                    if (tt.getCourse(c.getUniqueId()) == null)
                        tt.addCourse(c);
                });
            }
        }

        return tt;
    }

    private Collection<Course> retrieveCourses(Term term, Department department) throws IOException {

        boolean isSummer = term.getTermId() == TermClassifier.FULL_SUMMER;

        Collection<Course> courses = new ArrayList<>();

        RestResponse response = RestRequest.post(TIMETABLE_URL)
                .setFormParameter("sess", term.getKey())
                .setFormParameter("course", department.getCode())
                .setFormParameter("course2", "")
                .setFormParameter("submit", "Display by Discipline").run();
        Document doc = Jsoup.parse(response.getResponseString());

        Elements courseTable = doc.getElementById("timetable_section").select("tr");

        // A single row implies no course information.
        if (courseTable.size() <= 1) {
            return courses;
        }

        Course course = null;

        ListIterator<Element> rows = courseTable.select("tr").listIterator();

        while (rows.hasNext()) {

            ListIterator<Element> columns = rows.next().select("td").listIterator();
            Element firstColumn = columns.next();

            if (firstColumn.attr("colspan").equals("7")) {

                // Check if it is an enrolment  or informational note block.
                Elements notes;

                if (!(notes = firstColumn.select("div[name=Enr_Contr]")).isEmpty()
                        || !(notes = firstColumn.select("div[name=Inf_Cor]")).isEmpty()) {

                    if (course != null)
                        course.addNote(notes.first().html());
                    else
                        throw new NullPointerException("Attempted to add notes to a course that does not exist");

                } else {

                    String code = ParsingTools.sanitize(firstColumn.text());

                    // At this point, the column must be pointing to a course.
                    Matcher m = COURSE_CODE_DEP_MATCHER.matcher(code);

                    if (!m.find())
                        throw new IllegalArgumentException("Unmatchable course code pattern \"" + code + "\"");

                    double credits;

                    switch (m.group("credits")) {

                        case "Y":
                            credits = 1.0;
                            break;
                        case "H":
                            credits = 0.5;
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown credit signifier \""
                                    + m.group("credits") + "\"");
                    }

                    TermClassifier courseTerm;

                    switch (m.group("term")) {

                        case "F":

                            courseTerm = isSummer ? TermClassifier.SUMMER_ONE : TermClassifier.FALL;
                            break;

                        case "S":

                            courseTerm = isSummer ? TermClassifier.SUMMER_TWO : TermClassifier.SPRING;
                            break;

                        case "Y":

                            courseTerm = isSummer ? TermClassifier.FULL_SUMMER : TermClassifier.FULL_SCHOOL_YEAR;
                            break;

                        default:
                            throw new IllegalArgumentException("Unknown term signifier \"" + m.group("term") + "\"");
                    }

                    String courseCode = m.group("code");
                    String courseName = m.group("name");

                    if (course != null)
                        LOGGER.debug("\n" + course);

                    course = new Course(getSchool(), courseTerm, department, courseCode,
                            courseName.isEmpty() ? null : courseName, credits);
                    courses.add(course);
                }

                // If the next row contains the section headings, then rewind.
                if (!rows.next().select("td").stream().anyMatch(x -> x.ownText().equals("Meeting Section")))
                    rows.previous();

            } else {

                // Rewind the row.
                rows.previous();
                parseSections(course, rows);
            }
        }

        return courses;
    }

    private RepeatingPeriod parseTemporalData(TermClassifier term, String day,
                                              String startTimeString, String endTimeString,
                                              String locationString,
                                              String instructorString, String noteString) {

        RepeatingPeriod rp = new RepeatingPeriod(term);

        if (!day.isEmpty()) {

            DayOfWeek dow;

            switch (day) {

                case "MO":

                    dow = DayOfWeek.MONDAY;
                    break;

                case "TU":

                    dow = DayOfWeek.TUESDAY;
                    break;

                case "WE":

                    dow = DayOfWeek.WEDNESDAY;
                    break;

                case "TH":

                    dow = DayOfWeek.THURSDAY;
                    break;

                case "FR":

                    dow = DayOfWeek.FRIDAY;
                    break;

                case "SA":

                    dow = DayOfWeek.SATURDAY;
                    break;

                default:
                    throw new IllegalArgumentException("Unknown day-of-week signifier \"" + day + "\"");
            }

            int[] startTimeHourMinute =
                    Arrays.stream(startTimeString.split(":")).mapToInt(Integer::parseInt).toArray();

            LocalTime startTime = LocalTime.of(startTimeHourMinute[0], startTimeHourMinute[1]);

            int[] endTimeHourMinute =
                    Arrays.stream(endTimeString.split(":")).mapToInt(Integer::parseInt).toArray();

            LocalTime endTime = LocalTime.of(endTimeHourMinute[0], endTimeHourMinute[1]);

            rp.setTime(dow, startTime, endTime);
        }

        if (!locationString.isEmpty())
            rp.setRoom(locationString);

        if (!instructorString.isEmpty())
            rp.addSupervisors(instructorString);

        if (!noteString.isEmpty())
            rp.addNotes(noteString);

        return rp;
    }

    private void parseSections(Course course, ListIterator<Element> rows) {

        Section section = null;

        do {

            Element row = rows.next();

            if (!row.attr("style").equals("background-color: rgb(231, 234, 239);")) {
                rows.previous();
                return;
            }

            Iterator<Element> columns = row.select("td").iterator();

            String sectionName = ParsingTools.sanitize(columns.next().ownText());
            String sectionDay = ParsingTools.sanitize(columns.next().ownText());
            String sectionStart = ParsingTools.sanitize(columns.next().ownText());
            String sectionEnd = ParsingTools.sanitize(columns.next().ownText());
            String sectionLocation = ParsingTools.sanitize(columns.next().ownText());
            String sectionInstructors = ParsingTools.sanitize(columns.next().ownText());
            String sectionNotes = ParsingTools.sanitize(columns.next().ownText());

            if (!sectionName.isEmpty()) {

                section = Section.fromName(sectionName).setOnline(sectionName.endsWith("99"));
                SectionType sectionType = course.getSectionType(sectionName.substring(0, 3));

                if (sectionType == null || !sectionType.getSectionKeys().contains(section.getId())) {
                    course.addSection(sectionName.substring(0, 3), section);
                } else {
                    LOGGER.warn("Duplicate section \"" + section.getId() + "\" detected. Skipping duplicate...");
                }
            }

            if (section == null)
                throw new NullPointerException("Attempted to set term data into a null section");
            else
                section.addPeriod(parseTemporalData(course.getTerm(), sectionDay,
                        sectionStart, sectionEnd, sectionLocation, sectionInstructors, sectionNotes));

        } while (rows.hasNext());
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {

        Elements sessionInputs = Jsoup.parse(RestRequest.get(TIMETABLE_URL).run()
                .getResponseString()).select("input[name=sess]");

        Set<Term> terms = new HashSet<>();

        for (Element sessionInput : sessionInputs) {

            String id = sessionInput.attr("value");
            String value = ParsingTools.sanitize(sessionInput.parent().ownText());

            terms.add(Term.findProbableTerm(value, id));
        }

        return terms;
    }
}
