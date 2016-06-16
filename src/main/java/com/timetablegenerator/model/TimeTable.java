package com.timetablegenerator.model;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@EqualsAndHashCode(exclude={"lastUpdate"})
public class TimeTable implements Comparable<TimeTable>, Diffable<TimeTable> {

    @Getter private final School school;
    @Getter private final Term term;
    @Getter private final ZonedDateTime lastUpdate;

    private final Map<String, Course> courses = new ConcurrentSkipListMap<>();

    private TimeTable(School school, Term term, ZonedDateTime parseTime) {
        this.school = school;
        this.term = term;
        this.lastUpdate = parseTime;
    }

    public static TimeTable of(@NonNull School school, @NonNull Term term) {
        return new TimeTable(school, term, ZonedDateTime.now(ZoneOffset.UTC));
    }

    public static TimeTable of(@NonNull School school, @NonNull Term term,
                               @NonNull ZonedDateTime parseTime) {
        return new TimeTable(school, term, parseTime);
    }

    public TimeTable addCourse(Course c) {
        String id = c.getUniqueId();
        if (this.courses.putIfAbsent(id, c) != null) {
            throw new IllegalStateException("Attempted to insert multiple courses with the ID \"" + id + "\".");
        }
        return this;
    }

    public Optional<Course> getCourse(String id) {
        return this.courses.containsKey(id) ? Optional.of(this.courses.get(id)) : Optional.empty();
    }

    public Collection<Course> getCourses() {
        return this.courses.values();
    }

    @Override
    public String toString() {
        return "[school: " + this.school.getId() + ", " +
                "term: " + this.term + ", " +
                "departments: " + courses.values().stream()
                .collect(Collectors.groupingBy(Course::getDepartment)).size() + ", " +
                "courses: " + courses.size() + ", " +
                "last_update: " + lastUpdate.toString() + "]";
    }

    @Override
    public int compareTo(@Nonnull TimeTable that) {
        if (!this.school.equals(that.school)){
            return this.school.compareTo(that.school);
        }
        if (!this.term.equals(that.term)){
            return this.term.compareTo(that.term);
        }
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String getDeltaId() {
        return school.getId() + "/" + this.term.getYear() + "/" + this.term.getTermDefinition().getCode();
    }

    @Override
    public StructureDelta findDifferences(TimeTable that) {

        if (!this.term.equals(that.term)) {
            throw new IllegalArgumentException("Timetables are not from the same term: \""
                    + this.term + "\" and \"" + that.term + "\"");
        }

        if (this.school != that.school) {
            throw new IllegalArgumentException("Timetables are not from the same school: \""
                    + this.school.getId() + "\" and \"" + that.school.getId() + "\"");
        }

        final StructureDelta delta = StructureDelta.of(PropertyType.TIMETABLE, this);

        // Record courses that were added.
        that.courses.values().stream()
                .filter(x -> !this.courses.keySet().contains(x.getUniqueId()))
                .forEach(x -> delta.addAdded(PropertyType.COURSE, x));

        // Record courses that were removed.
        this.courses.values().stream()
                .filter(x -> !that.courses.keySet().contains(x.getUniqueId()))
                .forEach(x -> delta.addRemoved(PropertyType.COURSE, x));

        // Record courses that changed.
        this.courses.values().stream()
                .map(Course::getUniqueId)
                .filter(x -> that.courses.keySet().contains(x))
                .filter(x -> !this.courses.get(x).equals(that.courses.get(x)))
                .forEach(x -> delta.addSubstructureChange(this.courses.get(x).findDifferences(that.courses.get(x))));

        return delta;
    }
}