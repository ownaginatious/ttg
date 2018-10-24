package com.timetablegenerator.scraper.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.Period;
import com.timetablegenerator.model.period.RepeatingPeriod;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public enum JsonGenerator {

    INSTANCE;

    private final DateTimeFormatter UPDATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm'Z'").withZone(ZoneOffset.UTC);

    private JsonObject serializePeriod(Period period){

        JsonObject periodJson = new JsonObject();

        periodJson.addProperty("term", period.getTerm().getId());

        period.isOnline().ifPresent(x -> periodJson.addProperty("online", x));
        period.getCampus().ifPresent(x -> periodJson.addProperty("campus", x));
        period.getRoom().ifPresent(x -> periodJson.addProperty("room", x));

        if (period.isScheduled()){

            if (period instanceof OneTimePeriod) {

                OneTimePeriod otp = (OneTimePeriod) period;

                periodJson.addProperty("start", otp.getStartDateTime().format(OneTimePeriod.DATETIME_FORMAT));
                periodJson.addProperty("end", otp.getEndDateTime().format(OneTimePeriod.DATETIME_FORMAT));

            } else {

                RepeatingPeriod rp = (RepeatingPeriod) period;

                if (rp.isScheduled()) {

                    periodJson.addProperty("day", rp.getDayOfWeek().getValue());
                    periodJson.addProperty("start", rp.getStartTime().format(RepeatingPeriod.TIME_FORMAT));
                    periodJson.addProperty("end", rp.getEndTime().format(RepeatingPeriod.TIME_FORMAT));
                }
            }
        }

        if (!period.getSupervisors().isEmpty()) {

            JsonArray supervisorsJson = new JsonArray();
            periodJson.add("supervisors", supervisorsJson);

            period.getSupervisors().forEach(x -> supervisorsJson.add(new JsonPrimitive(x)));
        }

        return periodJson;
    }

    private JsonObject serializeSection(Section section) {

        JsonObject sectionJson = new JsonObject();

        section.getSerialNumber().ifPresent(x -> sectionJson.addProperty("serial", x));

        section.isOnline().ifPresent(x -> sectionJson.addProperty("online", x));
        section.isCancelled().ifPresent(x -> sectionJson.addProperty("cancelled",x));
        section.isAlternating().ifPresent(x -> sectionJson.addProperty("alternating", x));

        section.isFull().ifPresent(x -> sectionJson.addProperty("section_full", x));
        section.getEnrollment().ifPresent(x -> sectionJson.addProperty("num_enrolled", x));
        section.getMaxEnrollment().ifPresent(x -> sectionJson.addProperty("max_enrolled", x));

        section.hasWaitingList().ifPresent(x -> sectionJson.addProperty("has_waitlist", x));
        section.getWaiting().ifPresent(x -> sectionJson.addProperty("num_waiting", x));
        section.getMaxWaiting().ifPresent(x -> sectionJson.addProperty("max_waiting", x));

        if (!section.getRepeatingPeriods().isEmpty()){

            JsonArray periodsJson = new JsonArray();
            sectionJson.add("r_periods", periodsJson);

            section.getRepeatingPeriods().forEach(x -> periodsJson.add(this.serializePeriod(x)));
        }

        if (!section.getOneTimePeriods().isEmpty()){

            JsonArray periodsJson = new JsonArray();
            sectionJson.add("s_periods", periodsJson);

            section.getOneTimePeriods().forEach(x -> periodsJson.add(this.serializePeriod(x)));
        }

        if (!section.getNotes().isEmpty()){

            JsonArray notesJson = new JsonArray();
            sectionJson.add("notes", notesJson);

            section.getNotes().forEach(x -> notesJson.add(new JsonPrimitive(x)));
        }

        return sectionJson;
    }

    private JsonObject serializeCourse(Course course) {

        JsonObject courseJson = new JsonObject();

        courseJson.addProperty("name", course.getName());
        courseJson.addProperty("department", course.getDepartment().getCode());
        courseJson.addProperty("code", course.getCode());
        courseJson.addProperty("credits", course.getCredits());
        courseJson.addProperty("term", course.getTerm().getId());

        if (course.getDescription() != null)
            courseJson.addProperty("description", course.getDescription());

        if (!course.getPrerequisites().isEmpty()) {

            JsonArray prerequisitesJson = new JsonArray();
            courseJson.add("prerequisites", prerequisitesJson);

            course.getPrerequisites().forEach(x -> prerequisitesJson.add(new JsonPrimitive(x.getUniqueId())));
        }

        if (!course.getAntirequisites().isEmpty()) {

            JsonArray antirequisitesJson = new JsonArray();
            courseJson.add("antirequisites", antirequisitesJson);

            course.getAntirequisites().forEach(x -> antirequisitesJson.add(new JsonPrimitive(x.getUniqueId())));
        }

        if (!course.getSectionTypes().isEmpty()) {

            JsonObject sectionTypesJson = new JsonObject();
            courseJson.add("sections", sectionTypesJson);

            for (String sectionTypeKey : course.getSectionTypes()) {

                JsonObject sectionTypeJson = new JsonObject();
                sectionTypesJson.add(sectionTypeKey, sectionTypeJson);

                SectionType sectionType = course.getSectionType(sectionTypeKey);

                for (String sectionKey : sectionType.getSectionKeys())
                    sectionTypeJson.add(sectionKey, serializeSection(sectionType.getSection(sectionKey)));
            }
        }

        if (!course.getNotes().isEmpty()){

            JsonArray notesJson = new JsonArray();
            courseJson.add("notes", notesJson);

            course.getNotes().forEach(x -> notesJson.add(new JsonPrimitive(x)));
        }

        return courseJson;
    }

    public JsonObject toJson(School school, Collection<TimeTable> timeTables) {

        JsonObject schoolObject = new JsonObject();

        schoolObject.addProperty("name", school.getSchoolName());
        schoolObject.addProperty("displays_dep_prefix", school.usesDepartmentPrefixes());

        JsonObject sectionTypesJson = new JsonObject();
        schoolObject.add("section_types", sectionTypesJson);

        for (String sectionTypeKey : school.getSectionTypeCodes())
            sectionTypesJson.addProperty(sectionTypeKey, school.getSectionTypeNameByCode(sectionTypeKey));

        JsonObject departmentsJson = new JsonObject();
        schoolObject.add("departments", departmentsJson);

        JsonObject timeTablesJson = new JsonObject();
        schoolObject.add("timetables", timeTablesJson);

        for (TimeTable tt : timeTables) {

            JsonObject yearJson;
            String yearKey = tt.getTerm().getYear().toString();

            if (!timeTablesJson.has(yearKey)) {

                yearJson = new JsonObject();
                timeTablesJson.add(yearKey, yearJson);

            } else
                yearJson = timeTablesJson.getAsJsonObject(yearKey);

            JsonObject timetableJson = new JsonObject();
            yearJson.add(Integer.toString(tt.getTerm().getTermId().getId()), timetableJson);

            timetableJson.addProperty("last_update", tt.getLastUpdate().format(UPDATE_TIME_FORMAT));

            JsonObject coursesJson = new JsonObject();
            timetableJson.add("courses", coursesJson);

            for (Course c : tt.getCourses()) {

                Department d = c.getDepartment();
                departmentsJson.addProperty(d.getCode(), d.getName());

                coursesJson.add(c.getUniqueId(), serializeCourse(c));
            }
        }

        return schoolObject;
    }
}