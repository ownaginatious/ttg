package com.timetablegenerator.model;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import java.util.concurrent.ConcurrentSkipListMap;

@EqualsAndHashCode(exclude={"lastUpdate"})
public class TimeTable implements Comparable<TimeTable>, Diffable<TimeTable> {

    @Getter private final School school;
    @Getter private final Term term;
    @Getter private final ZonedDateTime lastUpdate;

    private final Map<String, Course> courses = new ConcurrentSkipListMap<>();

    public TimeTable(@Nonnull School school, @Nonnull Term term) {

        this.school = school;
        this.term = term;
        this.lastUpdate = ZonedDateTime.now();
    }

    public TimeTable(@Nonnull School school, @Nonnull Term term,
                     @Nonnull ZonedDateTime parseDate) {

        this.school = school;
        this.term = term;
        this.lastUpdate = parseDate;
    }

    public void addCourse(Course c) {

        String id = c.getUniqueId();

        if (this.courses.putIfAbsent(id, c) != null)
            throw new IllegalStateException("Attempted to insert multiple courses with the ID \"" + id + "\".");
    }

    public void removeCourse(Course c) {
        this.courses.remove(c.getUniqueId());
    }

    public Course getCourse(String id) {
        return this.courses.get(id);
    }

    public Collection<Course> getCourses() {
        return this.courses.values();
    }

    @Override
    public String toString() {
        return "TERM: " + this.term + ", COURSE #: " + courses.size() +
                ", LAST_UPDATE: " + lastUpdate.toString();
    }

    @Override
    public int compareTo(@Nonnull TimeTable tt) {
        return term.compareTo(tt.term);
    }

    @Override
    public String getDeltaId(){
        return school.getSchoolId() + "/" + this.term.getYear() + "/" + this.term.getTermId().getId();
    }

    @Override
    public StructureChangeDelta findDifferences(TimeTable that) {

        if (!this.term.equals(that.term)) {
            throw new IllegalStateException("Timetables are not from the same term: \""
                    + this.term + "\" and \"" + that.term + "\"");
        }

        if (this.school != that.school) {
            throw new IllegalStateException("Timetables are not from the same school: \""
                    + this.school.getSchoolId() + "\" and \"" + that.school.getSchoolId() + "\"");
        }

        final StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.TIMETABLE, this);

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
                .forEach(x -> delta.addChange(this.courses.get(x).findDifferences(that.courses.get(x))));

        return delta;
    }
}