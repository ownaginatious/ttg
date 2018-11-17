package com.timetablegenerator.serializer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.timetablegenerator.model.Course;
import com.timetablegenerator.model.TimeTable;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeTableSerializer implements Serializer<TimeTable> {

    @JsonIgnore
    private static final DateTimeFormatter TIME_STAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;

    @JsonProperty("term") private String termId = null;
    @JsonProperty("lastUpdate") private String lastUpdate = null;

    @JsonDeserialize(contentAs = CourseSerializer.class)
    @JsonProperty("courses") private Map<String, Serializer<Course>> courses = null;

    @Override
    public Serializer<TimeTable> fromInstance(TimeTable instance) {

        this.termId = instance.getTerm().getUniqueId();
        this.lastUpdate = instance.getLastUpdate().format(TIME_STAMP_FORMAT);

        this.courses = instance.getCourses().stream()
                .collect(Collectors.toMap(Course::getUniqueId, x -> {
                    CourseSerializer serializer = new CourseSerializer();
                    return serializer.fromInstance(x);
                }));

        return this;
    }

    @Override
    public TimeTable toInstance(SerializerContext context) {

        TimeTable timetable = TimeTable.of(context.getSchool(), context.getTerm(this.termId),
                ZonedDateTime.parse(this.lastUpdate, TIME_STAMP_FORMAT));

        if (this.courses != null) {

            Map<String, Course> courses = this.courses.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().toInstance(context)));

            // Validate the course mapping.
            for (Map.Entry<String, Course> entry : courses.entrySet()) {
                if (entry.getKey().equals(entry.getValue().getUniqueId())) {
                    throw new IllegalStateException(
                            "Serialized time table has an invalid course ID -> course mapping: " +
                                    entry.getKey() + " to " + entry.getValue().getUniqueId());
                }
            }

            this.courses.values().stream();
        }

        return timetable;
    }
}