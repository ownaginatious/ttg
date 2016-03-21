package com.timetablegenerator.scraper.school.uottawa;

import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.Scraper;
import com.timetablegenerator.scraper.annotation.SectionMapping;
import com.timetablegenerator.scraper.utility.network.RestRequest;
import com.timetablegenerator.scraper.utility.network.RestResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SchoolConfig(
        name = "University of Ottawa", id = "uottawa",
        useDepartmentPrefixes = false,
        sections = {
                @SectionMapping(name = "Lecture", code = "LEC"),
                @SectionMapping(name = "Course has on-line/classroom activities", code = "HYB"),
                @SectionMapping(name = "Course entirely via Internet", code = "IN1"),
                @SectionMapping(name = "Discussion Group", code = "DGD"),
                @SectionMapping(name = "Seminar", code = "SEM"),
                @SectionMapping(name = "Tutorial", code = "TUT"),
                @SectionMapping(name = "Research", code = "REC"),
                @SectionMapping(name = "Laboratory", code = "LAB"),
                @SectionMapping(name = "Theory and laboratory", code = "TLB"),
                @SectionMapping(name = "Work Term", code = "STG"),
                @SectionMapping(name = "Videoconference course", code = "VDC"),
                @SectionMapping(name = "Audioconference course", code = "AUC")
        }
)
public class UniversityOfOttawaScraper extends Scraper {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Set<String> NON_COLLAPSING_SECTIONS = new HashSet<>(
            Arrays.asList("DGD", "LAB", "TUT")
    );

    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^(?<code>[A-Z]+[0-9]+[A-Z]?) (?<section>[A-Z0-9]+ )?.*");
    private static final Pattern SECTION_PATTERN = Pattern.compile("^(?<section>.+) (?<number>[0-9]+)$");
    private static final Pattern DAY_TIME_PATTERN =
            Pattern.compile("^(?<day>[A-Z][a-z]+) (?<shour>[0-9]{2}):(?<sminute>[0-9]{2}) - (?<ehour>[0-9]{2}):(?<eminute>[0-9]{2})$");

    private static final String UNAVAILABLE = "Not available at this time.";
    private static final String TBA_TIME = "00:00 - 00:00";
    private static final String ONLINE_INDICATOR = "Course entirely via Internet";

    private static final String TIMETABLE_ROOT = "https://web30.uottawa.ca/v3/SITS/timetable/Search.aspx";
    private static final String RESULTS_ROOT = "https://web30.uottawa.ca/v3/SITS/timetable/SearchResults.aspx";
    private static final String PRINT_ROOT = "https://web30.uottawa.ca/v3/SITS/timetable/PrintSchedule.aspx";

    private RestRequest addState(Element responseElement, RestRequest req) {

        String eventValidation = responseElement.getElementById("__EVENTVALIDATION").attr("value");
        String viewState = responseElement.getElementById("__VIEWSTATE").attr("value");

        return req.setFormParameter("__EVENTVALIDATION", eventValidation).setFormParameter("__VIEWSTATE", viewState);
    }

    @Override
    public TimeTable retrieveTimetable(Term term) throws IOException {

        LOGGER.info("Beginning extraction of the " + term + " term for University of Ottawa...");
        RestResponse rr = RestRequest.get(TIMETABLE_ROOT).run();

        Element searchPageElement = Jsoup.parse(rr.getResponseString());

        Collection<Department> departments =  searchPageElement.getElementById("ctl00_MainContentPlaceHolder_Basic_SubjectDropDown")
                .select("option").stream().filter(e -> e.attr("value") != null && !e.attr("value").isEmpty())
                .map(e -> new Department(e.attr("value"), e.text())).collect(Collectors.toList());

        TimeTable tt = new TimeTable(getSchool(), term);

        int i = 0;

        for (Department d : departments){

            LOGGER.info("Retrieving courses under department " + d + " [" + ++i + "/" + departments.size() + "]...");

            rr = addState(searchPageElement, rr.nextPost(TIMETABLE_ROOT)
                    .setFormParameter("ctl00$MainContentPlaceHolder$Basic_SessionDropDown", term.getKey())
                    .setFormParameter("ctl00$MainContentPlaceHolder$Cancel_SessionDropDown", term.getKey())
                    .setFormParameter("ctl00$MainContentPlaceHolder$Basic_SubjectDropDown", d.getCode())
                    .setFormParameter("ctl00$MainContentPlaceHolder$Basic_Button", "Search")).run();

            boolean hasMore;

            Element responseElement;

            Map<String, String> courseNameMap = new HashMap<>();

            while (true) {

                responseElement = Jsoup.parse(rr.getResponseString());

                hasMore = responseElement.select("a").stream().anyMatch(x -> x.text().equalsIgnoreCase("Next"));

                responseElement.select("table.result-table > tbody > tr:not(.results-header)")
                        .forEach(x -> courseNameMap.put(x.select(".CourseCode").text(), x.select(".CourseTitle").text()));

                if (!hasMore)
                    break;

                LOGGER.debug(" >> Getting more data for department " + d.getCode() + "...");

                rr = addState(responseElement, rr.nextPost(RESULTS_ROOT)
                        .setFormParameter("__EVENTTARGET", "ctl00$MainContentPlaceHolder$ctl05")).run();
            }

            LOGGER.debug(" >> No more data for department " + d.getCode() + "!");

            Collection<Course> discoveredCourses =
                    Jsoup.parse(rr.nextGet(PRINT_ROOT).run().getResponseString()).select("#main-content div.schedule")
                            .stream().map(c -> parseCourseData(term, d, courseNameMap, c))
                            .flatMap(Collection::stream).collect(Collectors.toList());

            LOGGER.info(" >> " + discoveredCourses.size() + " courses discovered.");

            discoveredCourses.forEach(tt::addCourse);
        }

        return tt;
    }

    private Collection<Course> parseCourseData(Term term, Department department,
                                               Map<String, String> courseNameMap, Element courseElement) {

        Collection<Course> courses = new ArrayList<>();

        for (Element tableElement : courseElement.select("table.display > tbody")) {

            ListIterator<Element> rowIterator = tableElement.select("tr:not(.footer)").listIterator();

            // Throw away the header row.
            rowIterator.next();

            String courseCodeString = rowIterator.next().select(".Section").text();

            Matcher m = COURSE_CODE_PATTERN.matcher(courseCodeString);

            if (!m.find())
                throw new IllegalArgumentException("Cannot parse course code from \"" + courseCodeString + "\"");

            String courseCode = m.group("code");
            String courseSection = m.group("section") == null ? "" : m.group("section").trim();

            String courseName = courseNameMap.get(courseCode);

            if (courseName == null)
                throw new IllegalStateException("No matching course name for course code \"" + courseCode + "\"");

            // FIXME: Find out how many credits each course is actually worth.
            Course c = new Course(getSchool(), term.getTermId(), department,
                    courseCode + " " + courseSection, courseName, 3.0);

            courses.add(c);

            rowIterator.previous();

            while (rowIterator.hasNext()) {

                Element rowElement = rowIterator.next();

                String activityString = rowElement.select(".Activity").text();
                String dayTimeString = rowElement.select(".Day").text();
                String placeString = rowElement.select(".Place").text();
                String professorString = rowElement.select(".Professor").text();

                String sectionCode;
                String sectionNumber;

                boolean courseIsOnline = activityString.startsWith(ONLINE_INDICATOR);

                m = SECTION_PATTERN.matcher(activityString);

                if (!m.find())
                    throw new IllegalArgumentException("Cannot parse section type from \"" + activityString + "\"");

                sectionCode = getSchool().getSectionTypeCodeByName(m.group("section").trim());
                sectionNumber = m.group("number");

                Section s;

                if (!NON_COLLAPSING_SECTIONS.contains(sectionCode)
                        && c.getSectionType(sectionCode) != null
                        && c.getSectionType(sectionCode).getSection("1") != null)
                    s = c.getSectionType(sectionCode).getSection("1");
                else {
                    s = Section.fromName(sectionNumber);
                    c.addSection(sectionCode, s);
                }

                RepeatingPeriod rp = new RepeatingPeriod(term.getTermId());

                if (!dayTimeString.equals(TBA_TIME) && !courseIsOnline) {

                    m = DAY_TIME_PATTERN.matcher(dayTimeString);

                    if (!m.find())
                        throw new IllegalArgumentException("Cannot parse day time from \"" + dayTimeString + "\"");

                    String dayString = m.group("day");
                    DayOfWeek dow;

                    switch (dayString.substring(0, 2)) {

                        case "Mo":
                            dow = DayOfWeek.MONDAY;
                            break;
                        case "Tu":
                            dow = DayOfWeek.TUESDAY;
                            break;
                        case "We":
                            dow = DayOfWeek.WEDNESDAY;
                            break;
                        case "Th":
                            dow = DayOfWeek.THURSDAY;
                            break;
                        case "Fr":
                            dow = DayOfWeek.FRIDAY;
                            break;
                        case "Sa":
                            dow = DayOfWeek.SATURDAY;
                            break;
                        case "Su":
                            dow = DayOfWeek.SUNDAY;
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown day of week signifier \"" + dayString + "\"");
                    }

                    rp.setTime(dow, LocalTime.of(Integer.parseInt(m.group("shour")), Integer.parseInt(m.group("sminute"))),
                            LocalTime.of(Integer.parseInt(m.group("ehour")), Integer.parseInt(m.group("eminute"))));
                }

                if (!professorString.equals(UNAVAILABLE))
                    rp.addSupervisors(professorString);

                if (!courseIsOnline)
                    rp.setRoom(placeString);

                s.setOnline(courseIsOnline);

                s.addPeriod(rp);
            }
        }

        // Make all the courses anti-requisites of one another.
        courses.forEach(x -> courses.stream().filter(y -> x != y).forEach(x::addAntirequisite));

        return courses;
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {

        LOGGER.info("Retrieving terms for University of Ottawa");
        RestResponse rr = RestRequest.get(TIMETABLE_ROOT).run();

        Element termsElement = Jsoup.parse(rr.getResponseString());

        Set<Term> terms = new HashSet<>();

        termsElement.getElementById("ctl00_MainContentPlaceHolder_Basic_SessionDropDown")
                .select("option").stream().filter(e -> e.attr("value") != null && !e.attr("value").isEmpty())
                .forEach(e -> terms.add(Term.findProbableTerm(e.text(), e.attr("value"))));

        return terms;
    }
}