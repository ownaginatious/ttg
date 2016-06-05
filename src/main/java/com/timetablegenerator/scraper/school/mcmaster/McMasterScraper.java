package com.timetablegenerator.scraper.school.mcmaster;

import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.Period;
import com.timetablegenerator.model.period.RepeatingPeriod;

import com.timetablegenerator.scraper.*;
import static com.timetablegenerator.scraper.annotation.LegacyMapping.LegacyType.*;

import com.timetablegenerator.scraper.annotation.LegacyConfig;
import com.timetablegenerator.scraper.annotation.LegacyMapping;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.annotation.SectionMapping;
import com.timetablegenerator.scraper.utility.ParsingTools;
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
        name = "McMaster University", id = "mcmaster",
        useDepartmentPrefixes = true,
        sections = {
                @SectionMapping(name = "Core", code = "C"),
                @SectionMapping(name = "Lab", code = "L"),
                @SectionMapping(name = "Tutorial", code = "T")
        },
        legacy = @LegacyConfig(
                year = 2016, term = TermClassifier.FULL_SCHOOL_YEAR,
                mapping = {
                        @LegacyMapping(from = "C", to = CORE),
                        @LegacyMapping(from = "L", to = LAB),
                        @LegacyMapping(from = "T", to = TUTORIAL)
                }
        )
)
public class McMasterScraper extends Scraper {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Pattern COURSE_INFO_PATTERN = Pattern.compile(
            "^[A-Z]+\\s+(?<code>[1-9][A-Z]{1,2}(?<credits>[0-9]{1,2})[A-Z]?) - (?<name>.+)$");

    private static final Pattern SECTION_CODE_PATTERN = Pattern.compile(
            "^(?<name>[A-Z0-9]+)-(?<type>[A-Z]+)\\((?<serial>[0-9]+)\\)$");

    private static final String SEARCH_BLOCKING_METHOD =
            "Your search will return over 50 classes, would you like to continue?";

    RestResponse performAuthentication() throws IOException {

        LOGGER.info("Initializing scrape to McMaster University timetable data...");
        LOGGER.info("Authenticating into MOSAIC...");

        RestResponse rr = RestRequest.get("https://epprd.mcmaster.ca/psp/prepprd/EMPLOYEE/EMPL/").run()
                .nextPost("https://epprd.mcmaster.ca/psp/prepprd/EMPLOYEE/EMPL/")
                .allowInvalidCertificates(true)
                .setFormParameter("userid", "dixond2")
                .setFormParameter("pwd", "Hv6eVW2NKzMgUydESdgn")
                .setFormParameter("Submit", "Sign+In")
                .setQueryParameter("cmd", "login").run();

        LOGGER.info("Navigating to the search screen...");

        return rr.nextGet("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                .setQueryParameter("Page", "SSR_CLSRCH_ENTRY")
                .setQueryParameter("Action", "U")
                .setQueryParameter("ExactKeys", "Y")
                .setQueryParameter("TargetFrameName", "None").run();
    }

    @Override
    public TimeTable retrieveTimetable(Term term) throws IOException {

        TimeTable timeTable = new TimeTable(getSchool(), term);

        String[] components = timeTable.getTerm().getKey().split(":");

        // For now, this notation always signifies the full year.
        if (components.length > 1) {
            if (components.length == 2 && term.getTermId() == TermClassifier.FULL_SCHOOL_YEAR) {

                loadSubTimetable(timeTable, new Term(TermClassifier.FALL, term.getYear(), components[0]));
                loadSubTimetable(timeTable, new Term(TermClassifier.SPRING, term.getYear() + 1, components[1]));

                Collection<Course> toAdd = new ArrayList<>();
                Collection<Course> toRemove = new ArrayList<>();

                // Perform the complex recombination.
                for (Course course : timeTable.getCourses()) {

                    if (!course.getCode().endsWith("A") || course.getTerm() != TermClassifier.FALL)
                        continue;

                    toRemove.add(course);

                    Course replacement = new Course(getSchool(), TermClassifier.FULL_SCHOOL_YEAR,
                            course.getDepartment(), course.getCode(), course.getName(),
                            course.getCredits());

                    replacement.setDescription(course.getDescription());
                    course.getAntirequisites().forEach(replacement::addAntirequisite);
                    course.getPrerequisites().forEach(replacement::addPrerequisite);
                    course.getCrossListings().forEach(replacement::addCrossListing);

                    toAdd.add(replacement);

                    String newCourseCode = course.getCode()
                            .substring(0, course.getCode().length() - 1) + "B";

                    // Determine if the course has a counter part.
                    String counterPartId = new Course(getSchool(), TermClassifier.SPRING, course.getDepartment(),
                            newCourseCode, course.getName(), 0.0).getUniqueId();

                    Course counterPart = timeTable.getCourse(counterPartId);

                    if (counterPart == null) { // Extend the term to cover the next one as well.

                        for (String sectionTypeKey : getSchool().getSectionTypeCodes()) {

                            SectionType sectionType = course.getSectionType(sectionTypeKey);

                            if (sectionType == null)
                                continue;

                            for (String sectionKey : sectionType.getSectionKeys()) {

                                Section oldSection = sectionType.getSection(sectionKey);
                                Section newSection = Section.fromName(sectionKey);

                                oldSection.isAlternating().ifPresent(newSection::setAlternating);
                                oldSection.isOnline().ifPresent(newSection::setOnline);
                                oldSection.getSerialNumber().ifPresent(newSection::setSerialNumber);

                                replacement.addSection(sectionTypeKey, newSection);

                                for (RepeatingPeriod oldRepeating : oldSection.getRepeatingPeriods()) {

                                    RepeatingPeriod newRepeating = new RepeatingPeriod(TermClassifier.FULL_SCHOOL_YEAR);

                                    if (oldRepeating.isScheduled())
                                        newRepeating.setTime(oldRepeating.getDayOfWeek(),
                                                oldRepeating.getStartTime(), oldRepeating.getEndTime());

                                    oldRepeating.isOnline().ifPresent(newRepeating::setOnline);
                                    oldRepeating.getRoom().ifPresent(newRepeating::setRoom);
                                    newRepeating.addSupervisors(oldRepeating.getSupervisors());
                                    newRepeating.addNotes(oldRepeating.getNotes());

                                    newSection.addPeriod(newRepeating);
                                }
                            }
                        }

                    } else { // Merge the second term into this one.

                        // Remove part B as well. It is no longer needed.
                        toRemove.add(counterPart);

                        for (String sectionTypeKey : getSchool().getSectionTypeCodes()) {

                            // Check the A section.
                            SectionType sectionType = course.getSectionType(sectionTypeKey);

                            if (sectionType != null)
                                for (String sectionId : sectionType.getSectionKeys())
                                    replacement.addSection(sectionTypeKey, sectionType.getSection(sectionId));

                            // Check the B section.
                            sectionType = counterPart.getSectionType(sectionTypeKey);

                            if (sectionType != null) {
                                for (String sectionId : sectionType.getSectionKeys()) {

                                    Section termBSection = sectionType.getSection(sectionId);
                                    Section replacementSection = replacement.getSectionType(sectionTypeKey).getSection(sectionId);

                                    if (replacementSection == null)
                                        replacement.addSection(sectionTypeKey, termBSection);
                                    else
                                        termBSection.getRepeatingPeriods().forEach(replacementSection::addPeriod);
                                }
                            }
                        }
                    }
                }

                toRemove.forEach(timeTable::removeCourse);
                toAdd.forEach(timeTable::addCourse);

            } else
                throw new UnsupportedOperationException("Unsure how to subdivide term type \""
                        + term.getTermId() + "\" into " + components.length + " parts");
        } else
            loadSubTimetable(timeTable, term);

        return timeTable;
    }

    private void loadSubTimetable(TimeTable timeTable, Term term) throws IOException {

        LOGGER.info("Initiating extraction for McMaster University term: " + term.toString() + "...");

        // Retrieve all course content.
        RestResponse rr = this.performAuthentication();

        int i = 1;

        Collection<Department> departments = McMasterDepartmentScraper.INSTANCE.getDepartments();

        for (Department department : departments) {

            LOGGER.info("Retrieving data for department ["
                    + department.getCode() + " -> "
                    + department.getName() + "] {" + i++ + "/" + departments.size() + "}...");

            RestRequest req =
                    rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/"
                            + "HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                            .setFormParameter("ICAction", "CLASS_SRCH_WRK2_SSR_PB_CLASS_SRCH")
                            .setFormParameter("ICActionPrompt", "false")
                            .setFormParameter("ICFind", "")
                            .setFormParameter("ICAddCount", "")
                            .setFormParameter("ICAPPCLSDATA", "")
                            .setFormParameter("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$155$", "9999")
                            .setFormParameter("CLASS_SRCH_WRK2_INSTITUTION$41$", "MCMST")
                            .setFormParameter("MCM_DERIVED_CE_ACAD_CAREER", "UGRD")
                            .setFormParameter("CLASS_SRCH_WRK2_STRM$45$", term.getKey())
                            .setFormParameter("SSR_CLSRCH_WRK_SUBJECT$75$$0", department.getCode())
                            .setFormParameter("SSR_CLSRCH_WRK_SSR_EXACT_MATCH1$1", "E")
                            .setFormParameter("SSR_CLSRCH_WRK_CATALOG_NBR$1", "")
                            .setFormParameter("SSR_CLSRCH_WRK_SSR_OPEN_ONLY$chk$3", "N")
                            .setFormParameter("SSR_CLSRCH_WRK_CLASS_NBR$4", "")
                            .setFormParameter("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$183$", "9999");

            rr = req.run();

            // Handle the situation where a warning message about search returning a lot fromName results comes up.
            if (rr.getResponseString().contains(SEARCH_BLOCKING_METHOD)) {

                LOGGER.info("Navigating through the large result set screen...");

                rr = rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                        .setFormParameter("ICAction", "#ICSave").run();
            }

            Element resultsData = Jsoup.parse(rr.getResponseString().replace("<![CDATA[", "").replace("]]>", ""));

            if (resultsData.getElementById("ACE_$ICField229$0") != null) {

                LOGGER.info("Pulling course data...");

                Collection<Course> courses = parseCourses(term, department, resultsData);
                courses.forEach(timeTable::addCourse);

                LOGGER.info("Discovered [" + courses.size() + "] courses.");

                // Go back to the search page.
                rr = rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                        .setFormParameter("ICAction", "CLASS_SRCH_WRK2_SSR_PB_NEW_SEARCH")
                        .setFormParameter("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$5$", "9999")
                        .setFormParameter("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$207$", "9999").run();

            } else
                LOGGER.info("No courses listed under this department.");
        }
    }

    public Collection<Course> retrieveDepartment(Term term, Department department) throws IOException {

        LOGGER.info("Retrieving courses for McMaster University department \"" + department.getCode()
                + "\" and term: " + term.toString() + "...");

        // Retrieve all course content.
        RestResponse rr = this.performAuthentication();

        LOGGER.info("Initiating search...");

        RestRequest req =
                rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/"
                        + "HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                        .setFormParameter("ICAction", "CLASS_SRCH_WRK2_SSR_PB_CLASS_SRCH")
                        .setFormParameter("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$155$", "9999")
                        .setFormParameter("CLASS_SRCH_WRK2_INSTITUTION$41$", "MCMST")
                        .setFormParameter("MCM_DERIVED_CE_ACAD_CAREER", "UGRD")
                        .setFormParameter("CLASS_SRCH_WRK2_STRM$45$", term.getKey())
                        .setFormParameter("SSR_CLSRCH_WRK_SUBJECT$75$$0", department.getCode())
                        .setFormParameter("SSR_CLSRCH_WRK_SSR_EXACT_MATCH1$1", "E")
                        .setFormParameter("SSR_CLSRCH_WRK_SSR_OPEN_ONLY$chk$3", "N")
                        .setFormParameter("DERIVED_SSTSNAV_SSTS_MAIN_GOTO$183$", "9999");

        rr = req.run();

        // Handle the situation where a warning message about search returning a lot fromName results comes up.
        if (rr.getResponseString().contains(SEARCH_BLOCKING_METHOD)) {

            LOGGER.info("Navigating through the large result set screen...");

            rr = rr.nextPost("https://csprd.mcmaster.ca/psc/prcsprd/EMPLOYEE/HRMS_LS/c/SA_LEARNER_SERVICES.CLASS_SEARCH.GBL")
                    .setFormParameter("ICAction", "#ICSave").run();
        }

        Element resultsData = Jsoup.parse(rr.getResponseString().replace("<![CDATA[", "").replace("]]>", ""));

        if (resultsData.getElementById("ACE_$ICField229$0") != null) {

            LOGGER.info("Pulling course data...");

            return parseCourses(term, department, resultsData);
        }

        return new ArrayList<>(0);
    }

    private Collection<Course> parseCourses(Term containingTerm, Department department, Element departmentSchedule) {

        List<Course> courses = new ArrayList<>();

        Element courseTitle;

        for (int i = 0; (courseTitle = departmentSchedule.getElementById("win0divDERIVED_CLSRCH_DESCR200$" + i)) != null; i++) {

            TermClassifier currentSectionTerm = null;

            Section currentSection = null;

            String courseTitleString = ParsingTools.sanitize(courseTitle.text());

            // Parse the standard course information.
            Matcher courseData = COURSE_INFO_PATTERN.matcher(courseTitleString);

            String currentSectionTitle = null;
            Boolean currentSectionFull = null;

            final Map<TermClassifier, Course> currentCourseMultiplexer = new EnumMap<>(TermClassifier.class);

            String currentCourseName;
            String currentCourseCode;
            double currentCourseCredits;

            if (courseData.find()) {

                currentCourseName = courseData.group("name");
                currentCourseCode = courseData.group("code");
                currentCourseCredits = Double.parseDouble(courseData.group("credits"));

            } else {
                throw new IllegalArgumentException("Unable to parse course information from \""
                        + courseTitleString + "\"");
            }

            for (Element e : departmentSchedule.getElementById("ACE_$ICField237$" + i)
                    .select("tr, span.PSEDITBOX_DISPONLY, span.PSHYPERLINK > a")) {

                String id = e.attr("id");
                String tag = e.tag().toString();

                if (id == null)
                    continue;

                switch (tag) {

                    case "tr":

                        if (id.matches("trSSR_CLSRCH_MTG1\\$[0-9]+_row[1-9][0-9]*"))
                            parseAndAddTemporalData(currentSection, currentSectionTerm, e);
                        else
                            // Check to see if there is a status div.
                            for (Element subdiv : e.select("td > div")) {

                                id = subdiv.id();

                                if (id.matches("win0divDERIVED_CLSRCH_SSR_STATUS_LONG\\$[0-9]+")) {

                                    String statusText = ParsingTools.sanitize(subdiv.select("img").first().attr("alt"));

                                    switch (statusText) {

                                        case "Closed":
                                            currentSectionFull = true;
                                            break;

                                        case "Open":
                                            currentSectionFull = false;
                                            break;

                                        default:
                                            throw new IllegalArgumentException("Unknown course status \""
                                                    + statusText + "\"");
                                    }

                                    break;
                                }
                            }

                        break;

                    case "a":

                        if (id.matches("DERIVED_CLSRCH_SSR_CLASSNAME_LONG\\$[0-9]+"))
                            currentSectionTitle = ParsingTools.sanitize(e.text());

                        break;

                    case "span":

                        if (id.matches("PSXLATITEM_XLATSHORTNAME\\$[0-9]+")) {

                            String termString = ParsingTools.sanitize(e.text()).trim();

                            switch (termString) {

                                case "Spring":
                                    currentSectionTerm = TermClassifier.SUMMER_ONE;
                                    break;
                                case "Summer":
                                    currentSectionTerm = TermClassifier.SUMMER_TWO;
                                    break;
                                case "Winter":
                                    currentSectionTerm = TermClassifier.SPRING;
                                    break;
                                case "Regular":
                                case "MTA":
                                case "MT End":
                                    currentSectionTerm = containingTerm.getTermId();
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown term type \"" + termString + "\"");
                            }

                            Matcher m = SECTION_CODE_PATTERN.matcher(currentSectionTitle);

                            String currentSectionType;

                            if (m.find()) {

                                switch (m.group("type")) {

                                    case "LEC":
                                    case "SEM":
                                    case "WRK":
                                    case "COP":
                                    case "IND":
                                    case "PRJ":
                                    case "EXC":
                                    case "PRA":
                                    case "THE":

                                        currentSectionType = "C";
                                        break;

                                    case "LAB":

                                        currentSectionType = "L";
                                        break;

                                    case "TUT":

                                        currentSectionType = "T";
                                        break;

                                    default:
                                        throw new IllegalArgumentException("Unknown section type \""
                                                + m.group("type") + "\"");
                                }

                            } else
                                throw new IllegalArgumentException("Unparsable section code pattern \""
                                        + currentSectionTitle + "\"");

                            currentSection = Section.fromName(m.group("name")).setSerialNumber(m.group("serial"))
                                    .setFull(currentSectionFull);

                            currentSectionTitle = null;
                            currentSectionFull = null;

                            // Check if the course it should go into exists
                            Course parentCourse = currentCourseMultiplexer.get(currentSectionTerm);

                            if (parentCourse == null) {

                                parentCourse = new Course(getSchool(), currentSectionTerm, department,
                                        currentCourseCode, currentCourseName, currentCourseCredits);

                                currentCourseMultiplexer.put(currentSectionTerm, parentCourse);
                                courses.add(parentCourse);
                            }

                            parentCourse.addSection(currentSectionType, currentSection);
                        }

                        break;

                    default:
                        throw new IllegalArgumentException("Unknown tag '" + tag + "'");
                }
            }

            // Check if the course is a candidate for alternation.
            // Our rule will be, if the course has more than 2 lab/tutorial sections with the same suffix.
            // TODO: Keep a lookout for actual information source.

            for (Course c : currentCourseMultiplexer.values()) {

                SectionType labs = c.getSectionType("L");
                SectionType tutorials = c.getSectionType("T");

                if (labs != null && tutorials != null && (labs.getSectionKeys().size() > 2
                        && labs.getSectionKeys().size() == tutorials.getSectionKeys().size())) {

                    labs.getSectionKeys().forEach(x -> labs.getSection(x).setAlternating(true));
                    tutorials.getSectionKeys().forEach(x -> tutorials.getSection(x).setAlternating(true));
                }
            }
        }

        return courses;
    }

    private void parseAndAddTemporalData(Section section, TermClassifier term, Element e) {

        List<Period> periods = null;

        for (Element span : e.select("span.PSLONGEDITBOX")) {

            String id = ParsingTools.sanitize(span.id());

            if (id.trim().isEmpty())
                continue;

            if (id.matches("MTG_DAYTIME\\$[0-9]+")) {

                if (periods != null)
                    periods.forEach(section::addPeriod);

                periods = new ArrayList<>();

                String spanText = ParsingTools.sanitize(span.text());

                if (spanText.trim().equals("TBA")) {

                    periods.add(new RepeatingPeriod(term));
                    continue;
                }

                Set<DayOfWeek> days = new HashSet<>();
                String[] timeComponents = spanText.split(" ");

                for (int i = 0; i < timeComponents[0].length(); i += 2) {

                    String shortFormDay = timeComponents[0].substring(i, i + 2);

                    switch (shortFormDay) {
                        case "Mo":
                            days.add(DayOfWeek.MONDAY);
                            break;
                        case "Tu":
                            days.add(DayOfWeek.TUESDAY);
                            break;
                        case "We":
                            days.add(DayOfWeek.WEDNESDAY);
                            break;
                        case "Th":
                            days.add(DayOfWeek.THURSDAY);
                            break;
                        case "Fr":
                            days.add(DayOfWeek.FRIDAY);
                            break;
                        case "Sa":
                            days.add(DayOfWeek.SATURDAY);
                            break;
                        case "Su":
                            days.add(DayOfWeek.SUNDAY);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown day type \"" + shortFormDay + "\"");
                    }
                }

                String startTimeString = timeComponents[1];
                String endTimeString = timeComponents[3];

                String[] startTimeParts = startTimeString.replaceAll("[AP]M", "").split(":");
                String[] endTimeParts = endTimeString.replaceAll("[AP]M", "").split(":");

                LocalTime startTime = LocalTime.of(Integer.parseInt(startTimeParts[0]), Integer.parseInt(startTimeParts[1]));
                LocalTime endTime = LocalTime.of(Integer.parseInt(endTimeParts[0]), Integer.parseInt(endTimeParts[1]));

                if (startTimeString.contains("PM")) {
                    if (startTime.getHour() < 12)
                        startTime = startTime.plusHours(12);
                } else if (startTime.getHour() == 12)
                    startTime = startTime.withHour(0);

                if (endTimeString.contains("PM")) {
                    if (endTime.getHour() < 12)
                        endTime = endTime.plusHours(12);
                } else if (endTime.getHour() == 12)
                    endTime = endTime.withHour(0);

                for (DayOfWeek dow : days)
                    periods.add(new RepeatingPeriod(term).setTime(dow, startTime, endTime));

            } else if (span.id().matches("MTG_ROOM\\$[0-9]+")) {

                String location = ParsingTools.sanitize(span.text());

                if (location.equals("To be announced"))
                    location = "TBA";

                if (periods == null)
                    throw new NullPointerException("Attempted to set the room on null periods");

                final String finalLocation = location;
                periods.forEach(p -> p.setRoom(finalLocation));

            } else if (span.id().matches("MTG_INSTR\\$[0-9]+")) {

                if (periods == null)
                    throw new NullPointerException("Attempted to set the supervisors on null periods");

                final String[] supervisors = ParsingTools.sanitize(span.text()).split(",");
                periods.forEach(p -> p.addSupervisors(supervisors));
            }
        }

        if (periods != null)
            periods.forEach(section::addPeriod);
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {

        // Perform the login into MOSAIC and get the available terms.
        RestResponse rr = this.performAuthentication();

        Set<Term> probableTerms = new TreeSet<>();

        for (Element e : Jsoup.parse(rr.getResponseString())
                .getElementById("CLASS_SRCH_WRK2_STRM$45$").select("option")) {

            String key = e.attr("value");
            String text = ParsingTools.sanitize(e.ownText());

            if (key == null || key.trim().length() == 0)
                continue;

            Term t = Term.findProbableTerm(text, key);
            probableTerms.add(t);
        }

        // Check if full years are around in two pieces. If it is, recombine it.

        Set<Term> mergedTerms = new HashSet<>();

        Set<Term> fallTerms = probableTerms.stream()
                .filter(x -> x.getTermId() == TermClassifier.FALL).collect(Collectors.toSet());

        Set<Term> springTerms = probableTerms.stream()
                .filter(x -> x.getTermId() == TermClassifier.SPRING).collect(Collectors.toSet());

        for (Term fallTerm : fallTerms) {
            for (Term springTerm : springTerms) {
                if (springTerm.getYear() - fallTerm.getYear() == 1) {

                    probableTerms.add(new Term(TermClassifier.FULL_SCHOOL_YEAR, fallTerm.getYear(),
                            fallTerm.getKey() + ":" + springTerm.getKey()));

                    mergedTerms.add(fallTerm);
                    mergedTerms.add(springTerm);

                    break;
                }
            }
        }

        mergedTerms.forEach(probableTerms::remove);

        return probableTerms;
    }
}