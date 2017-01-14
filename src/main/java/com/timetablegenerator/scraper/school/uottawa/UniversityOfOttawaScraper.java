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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SchoolConfig(
        name = "University of Ottawa", id = "uottawa",
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

    //TODO: Activate after refactor
//    private static final Pattern COURSE_DATES_PATTERN =
//            Pattern.compile("\\((?<smonth>[A-Z][a-z]+) (?<sday>[0-9]+) - (?<emonth>[A-Z][a-z]+) (?<eday>[0-9]+)\\)");

    private static final Pattern DAY_TIME_PATTERN =
            Pattern.compile("^(?<day>[A-Z][a-z]+) " +
                    "(?<shour>[0-9]{1,2}):(?<sminute>[0-9]{2}) - (?<ehour>[0-9]{1,2}):(?<eminute>[0-9]{2})$");

    private static final String UNAVAILABLE = "Not available at this time.";

    private static final String TIMETABLE_ROOT = "https://web30.uottawa.ca/v3/SITS/timetable/Search.aspx";
    private static final String RESULTS_ROOT = "https://web30.uottawa.ca/v3/SITS/timetable/SearchResults.aspx";
    private static final String PRINT_ROOT = "https://web30.uottawa.ca/v3/SITS/timetable/PrintSchedule.aspx";

    private RestRequest addState(Element responseElement, RestRequest req) {

        String eventValidation = responseElement.getElementById("__EVENTVALIDATION").attr("value");
        String viewState = responseElement.getElementById("__VIEWSTATE").attr("value");

        return req.setFormParameter("__EVENTVALIDATION", eventValidation)
                .setFormParameter("__VIEWSTATE", viewState);
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
                    .setFormParameter("ctl00$MainContentPlaceHolder$Basic_TermDropDown", term.getKey())
                    .setFormParameter("ctl00$MainContentPlaceHolder$Basic_SubjectDropDown", d.getCode())
                    .setFormParameter("ctl00$MainContentPlaceHolder$Advance_CampusChoice", "UOTTA")
                    .setFormParameter("ctl00$MainContentPlaceHolder$Basic_Button", "Search")
            ).run();

            boolean hasMore;

            Element responseElement;

            Map<String, String> courseNameMap = new HashMap<>();
            Map<String, Course> discoveredCourses = new HashMap<>();

            while (true) {

                responseElement = Jsoup.parse(rr.getResponseString());

                hasMore = responseElement.select("a").stream().anyMatch(x -> x.text().equalsIgnoreCase("Next"));

                responseElement.select("table.result-table > tbody > tr:not(.results-header)")
                        .forEach(x -> courseNameMap.put(x.select(".CourseCode").text(), x.select(".CourseTitle").text()));

                // Parse the course data referenced by this page.
                Element schedulingElements = Jsoup.parse(rr.nextGet(PRINT_ROOT).run().getResponseString());

                for (Element schedulingElement : schedulingElements.select("#main-content div.schedule")) {
                    parseCourseData(term, d, courseNameMap, discoveredCourses, schedulingElement);
                }

                if (!hasMore) {
                    break;
                }

                LOGGER.debug(" >> Getting more data for department " + d.getCode() + "...");

                rr = addState(responseElement, rr.nextPost(RESULTS_ROOT)
                        .setFormParameter("__EVENTTARGET", "ctl00$MainContentPlaceHolder$ctl05")).run();
            }

            LOGGER.debug(" >> No more data for department " + d.getCode() + "!");
            LOGGER.info(" >> " + discoveredCourses.size() + " courses discovered.");

            discoveredCourses.values().forEach(tt::addCourse);
        }

        return tt;
    }

    private String[] parseCourseCodes(String courseCodeString) {

        courseCodeString = courseCodeString.trim();

        String courseName = courseCodeString.substring(0, courseCodeString.lastIndexOf(" "));
        String variant = null;
        String section = courseCodeString.substring(courseCodeString.lastIndexOf(" ") + 1);

        if (section.matches("^[A-Z]+[0-9]+")) {
            variant = section.split("[0-9]+")[0];
        }
        return new String[]{courseName, variant, section};
    }

    private void parseCourseData(Term term, Department department, Map<String, String> courseNameMap,
                                 Map<String, Course> courseMap, Element courseElement) {

        for (Element tableElement : courseElement.select("table.display > tbody")) {

            ListIterator<Element> rowIterator = tableElement.select("tr:not(.footer)").listIterator();

            // Throw away the header row.
            rowIterator.next();

            String courseCodeString = rowIterator.next().select(".Section").text();

            // Remove the course date range information.
            courseCodeString = courseCodeString.split("\\(")[0];
            String[] courseCodes = this.parseCourseCodes(courseCodeString);

            String courseCode = courseCodes[0];
            String courseVariant = courseCodes[1];
            String sectionCode = courseCodes[2];

            String courseName = courseNameMap.get(courseCode);

            if (courseName == null) {
                throw new IllegalStateException("No matching course name for course code \"" + courseCode + "\"");
            }

            // FIXME: Find out how many credits each course is actually worth.

            String courseLocalIdentifier = courseCode;
            if (courseVariant != null && courseVariant.trim().length() > 0){
                courseLocalIdentifier += " " + courseVariant;
            }

            Course c;

            if (courseMap.containsKey(courseLocalIdentifier)) {
                c = courseMap.get(courseLocalIdentifier);
            } else {
                c = new Course(getSchool(), term.getTermId(), department,
                        courseLocalIdentifier, courseName, 3.0);
                courseMap.put(courseLocalIdentifier, c);
            }

            rowIterator.previous();

            while (rowIterator.hasNext()) {

                Element rowElement = rowIterator.next();

                String sectionType = rowElement.select(".Activity").text();
                String dayTimeString = rowElement.select(".Day").text();
                String placeString = rowElement.select(".Place").text();
                String professorString = rowElement.select(".Professor").text();

                Section s;

                if (c.getSectionType(sectionType) != null
                        && c.getSectionType(sectionType).getSection(sectionCode) != null)
                    s = c.getSectionType(sectionType).getSection(sectionCode);
                else {
                    s = Section.fromName(sectionCode);
                    c.addSection(sectionType, s);
                }

                RepeatingPeriod rp = new RepeatingPeriod(term.getTermId());

                if (!dayTimeString.equals(UNAVAILABLE)) {

                    Matcher m = DAY_TIME_PATTERN.matcher(dayTimeString);

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

                if (!professorString.equals(UNAVAILABLE)) {
                    rp.addSupervisors(professorString);
                }

                if (!placeString.equals(UNAVAILABLE)) {
                    rp.setRoom(placeString);
                }

                s.addPeriod(rp);
            }
        }
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {

        LOGGER.info("Retrieving terms for University of Ottawa");
        RestResponse rr = RestRequest.get(TIMETABLE_ROOT).run();

        Element termsElement = Jsoup.parse(rr.getResponseString());

        Set<Term> terms = new HashSet<>();

        termsElement.getElementById("ctl00_MainContentPlaceHolder_Basic_TermDropDown")
                .select("option").stream().filter(e -> e.attr("value") != null && !e.attr("value").isEmpty())
                .forEach(e -> terms.add(Term.findProbableTerm(e.text(), e.attr("value"))));

        return terms;
    }
}