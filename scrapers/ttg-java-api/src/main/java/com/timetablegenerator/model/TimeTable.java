package com.timetablegenerator.model;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentSkipListMap;

public class TimeTable implements Comparable<TimeTable>, Diffable<TimeTable> {

    private final School school;
    private final Term term;
    private final ZonedDateTime lastUpdate;

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

    public Term getTerm() {
        return term;
    }

    public void addCourse(Course c) {

        String id = c.getUniqueId();

        if (this.courses.putIfAbsent(id, c) != null)
            throw new IllegalStateException("Attempted to insert multiple courses with the ID \"" + id + "\".");
    }

    public ZonedDateTime getLastUpdate() {
        return this.lastUpdate;
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
        return "TERM: " + this.term + ", COURSE #: " + courses.size()
                + ", LAST_UPDATE: " + lastUpdate.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof TimeTable))
            return false;

        TimeTable tt = (TimeTable) obj;

        if (!term.equals(tt.getTerm()))
            return false;

        Set<String> diffA = new HashSet<>(this.courses.keySet());
        Set<String> diffB = new HashSet<>(tt.courses.keySet());

        diffA.removeAll(tt.courses.keySet());
        diffB.removeAll(this.courses.keySet());

        if (diffA.size() > 0 || diffB.size() > 0)
            return false;

        for (String k : this.courses.keySet())
            if (!this.courses.get(k).equals(tt.courses.get(k)))
                return false;

        return true;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;

        result = prime * result + (this.courses.hashCode());
        result = prime * result + term.hashCode();

        return result;
    }

    @Override
    public int compareTo(@Nonnull TimeTable tt) {
        return term.compareTo(tt.term);
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

        final StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.TIMETABLE,
                school.getSchoolId() + "/" + this.term.getYear() + "/" + this.term.getTermId().getId());

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