package com.timetablegenerator.scraper.serialize;

import com.google.gson.*;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.scraper.annotation.LegacyMapping;
import com.timetablegenerator.scraper.annotation.LegacySchool;

import java.util.HashSet;
import java.util.Set;

/**
 * A class used for generating the legacy JSON format currently used in production. This format will be deprecated when
 * a new front end is created. Do not rely on this class for anything.
 */
public enum LegacyJsonGenerator {

    INSTANCE;

    private int approximateTerm(TermClassifier tc){

        int term;

        switch (tc){

            case FALL:
            case FALL_FIRST_QUARTER:
            case FALL_SECOND_QUARTER:
            case SUMMER_ONE:

                term = 1;
                break;

            case SPRING:
            case SPRING_FIRST_QUARTER:
            case SPRING_SECOND_QUARTER:
            case SUMMER_TWO:

                term = 2;
                break;

            case FULL_SUMMER:
            case FULL_SCHOOL_YEAR:

                term = 3;
                break;

            case UNSCHEDULED:

                term = 3;
                break;

            default:
                throw new UnsupportedOperationException("Term type \"" + tc.name()
                        + "\" cannot be used in the legacy model.");
        }

        return term;
    }

    private JsonObject serializeCourse(School school, LegacySchool legacyConfig, Course course) {

        // Create JSON object for course.
        JsonObject courseJson = new JsonObject();

        courseJson.addProperty("cod", course.getCode());
        courseJson.addProperty("n", course.getName());

        courseJson.addProperty("t", approximateTerm(course.getTerm()));
        courseJson.addProperty("a", true);
        courseJson.addProperty("tod", "D");
        courseJson.addProperty("u", course.getCredits());

        for (String sectionTypeKey : school.getSectionTypeCodes()){

            SectionType sectionType = course.getSectionType(sectionTypeKey);

            if (sectionType == null)
                continue;

            LegacyMapping.LegacyType legacyType = legacyConfig.getLegacyMapping(sectionTypeKey);

            // Skip the components that are not used in the legacy mapping
            if (legacyType == LegacyMapping.LegacyType.UNUSED)
                continue;

            String translatedType = legacyType.jsonKey;

            JsonArray sectionsWithinTypeArrayJson;

            if (courseJson.has(translatedType))
                sectionsWithinTypeArrayJson = courseJson.getAsJsonArray(translatedType);
            else {
                sectionsWithinTypeArrayJson = new JsonArray();
                courseJson.add(translatedType, sectionsWithinTypeArrayJson);
            }

            for (String sectionKey : sectionType.getSectionKeys()){

                JsonObject sectionJson = new JsonObject();
                sectionsWithinTypeArrayJson.add(sectionJson);

                Set<String> aggregatedSupervisors = new HashSet<>();
                JsonArray supervisorsArrayJson = new JsonArray();

                Section section = sectionType.getSection(sectionKey);

                section.isAlternating().ifPresent(x -> sectionJson.addProperty("EOW", x));

                if (section.getId().startsWith(sectionTypeKey))
                    sectionJson.addProperty("n", section.getId().substring(sectionTypeKey.length()));
                else
                    sectionJson.addProperty("n", section.getId());

                sectionJson.add("sups", supervisorsArrayJson);

                section.getSerialNumber().ifPresent(x -> sectionJson.addProperty("sn", x));

                JsonArray periodsJson = new JsonArray();
                sectionJson.add("ti", periodsJson);

                for(RepeatingPeriod repeatingPeriod : section.getRepeatingPeriods()){

                    JsonArray periodJson = new JsonArray();

                    if(repeatingPeriod.isScheduled()) {

                        periodJson.add(new JsonPrimitive(approximateTerm(repeatingPeriod.getTerm())));  // 0
                        periodJson.add(new JsonPrimitive(repeatingPeriod.getDayOfWeek().name()          // 1
                                .substring(0, 2).toLowerCase()));
                        periodJson.add(new JsonPrimitive(repeatingPeriod.getStartTime().getHour()));    // 2
                        periodJson.add(new JsonPrimitive(repeatingPeriod.getStartTime().getMinute()));  // 3
                        periodJson.add(new JsonPrimitive(repeatingPeriod.getEndTime().getHour()));      // 4
                        periodJson.add(new JsonPrimitive(repeatingPeriod.getEndTime().getMinute()));    // 5
                    }

                    repeatingPeriod.getRoom().ifPresent(x -> periodJson.add(new JsonPrimitive(x)));     // 6

                    aggregatedSupervisors.addAll(repeatingPeriod.getSupervisors());
                    periodsJson.add(periodJson);
                }

                aggregatedSupervisors.forEach(x -> supervisorsArrayJson.add(new JsonPrimitive(x)));
            }
        }

        return courseJson;
    }

    public JsonObject toJson(School school, LegacySchool legacyConfig, TimeTable tt) {

        JsonObject rootJson = new JsonObject();

        // Create the array of courses.
        JsonObject coursesJson = new JsonObject();
        rootJson.add("courses", coursesJson);

        for (Course c : tt.getCourses()) {

            // Get the department object.
            JsonArray departmentArrayJson;

            if (!coursesJson.has(c.getDepartment().getCode())) {
                departmentArrayJson = new JsonArray();
                coursesJson.add(c.getDepartment().getCode(), departmentArrayJson);
            } else
                departmentArrayJson = coursesJson.getAsJsonArray(c.getDepartment().getCode());

            // Create JSON object for course.
            departmentArrayJson.add(serializeCourse(school, legacyConfig, c));
        }

        // Create the named departments and removed those that are unused.
        JsonObject departmentsJson = new JsonObject();
        rootJson.add("departments", departmentsJson);

        tt.getCourses().stream().map(Course::getDepartment).distinct()
                .forEach(x -> departmentsJson.add(x.getName(), new JsonPrimitive(x.getCode())));

        return rootJson;
    }
}