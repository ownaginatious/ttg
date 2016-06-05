package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class TimeTableTests {

    private School school;
    private Term term = new Term(TermClassifier.FALL, 2016);
    private TimeTable tt;

    @Before
    public void setUp() {
        school = School.builder("test_name", "test_id")
                .withSection("Section Type A", "A")
                .withSection("Section Type B", "B")
                .withSection("Section Type C", "C")
                .withSection("Section Type D", "D").build();
        tt = TimeTable.of(this.school, this.term);
    }

    @Test
    public void creation() {
        assertTrue(tt.getCourses().isEmpty());
        assertThat(tt.getLastUpdate().until(ZonedDateTime.now(ZoneOffset.UTC), ChronoUnit.SECONDS),
                lessThan(1L));
        assertEquals(term, tt.getTerm());
        assertEquals(this.school, tt.getSchool());
        ZonedDateTime zdt = ZonedDateTime.now();
        tt = TimeTable.of(this.school, term, zdt);
        assertEquals(tt.getLastUpdate(), zdt);
    }

    @Test
    public void courseAddition() {
        Department d = Department.of("ABC", "Testing 123");
        Course c = Course.of(this.school, TermClassifier.FALL, d, "Code", "Name");
        Course c2 = Course.of(this.school, TermClassifier.FALL, d, "Code2", "Name");
        tt.addCourse(c).addCourse(c2);
        assertEquals(
                new HashSet<>(Arrays.asList(c, c2)),
                new HashSet<>(tt.getCourses()));

        assertEquals(c, tt.getCourse(c.getUniqueId()).orElse(null));
        assertEquals(c2, tt.getCourse(c2.getUniqueId()).orElse(null));

        assertFalse(tt.getCourse("no_such_id").isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateCourseAddition() {
        Department d = Department.of("ABC", "Testing 123");
        Course c = Course.of(this.school, TermClassifier.FALL, d, "Code", "Name");
        Course c2 = Course.of(this.school, TermClassifier.FALL, d, "Code", "Name");
        tt.addCourse(c).addCourse(c2);
    }

    @Test
    public void compare() {

        // By school.
        TimeTable tta = TimeTable.of(School.builder("a", "a").build(),
                this.term);
        TimeTable ttb = TimeTable.of(School.builder("b", "b").build(),
                this.term);
        assertThat(tta.compareTo(ttb), lessThan(0));

        // By term
        TimeTable tt1 = TimeTable.of(this.school,
                new Term(TermClassifier.FALL, 2015));
        TimeTable tt2 = TimeTable.of(this.school,
                new Term(TermClassifier.FULL_SUMMER, 2016));
        TimeTable tt3 = TimeTable.of(this.school,
                new Term(TermClassifier.FALL_FIRST_QUARTER, 2015));
        List<TimeTable> timetables = Arrays.asList(tt1, tt2, tt3);
        Collections.sort(timetables);
        assertEquals(Arrays.asList(tt3, tt1, tt2), timetables);

        // By else
        assertEquals(0, tta.compareTo(tta));
    }

    @Test
    public void noDiff() {
        Department d = Department.of("ABC", "Testing 123");
        Course c = Course.of(this.school, TermClassifier.FALL, d, "Code", "Name");
        Course c2 = Course.of(this.school, TermClassifier.FALL, d, "Code", "Name");
        TimeTable tt1 = TimeTable.of(this.school, this.term).addCourse(c);
        TimeTable tt2 = TimeTable.of(this.school, this.term).addCourse(c2);
        assertFalse(tt1.findDifferences(tt2).hasChanges());
    }

    @Test(expected = IllegalArgumentException.class)
    public void differentTermDiff() {
        TimeTable tt1 = TimeTable.of(this.school, new Term(TermClassifier.FALL, 2016));
        TimeTable tt2 = TimeTable.of(this.school, new Term(TermClassifier.FALL, 2017));
        tt1.findDifferences(tt2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void differentSchoolDiff() {
        School s1 = School.builder("School A", "A").build();
        School s2 = School.builder("School B", "B").build();
        TimeTable tt1 = TimeTable.of(s1, this.term);
        TimeTable tt2 = TimeTable.of(s2, this.term);
        tt1.findDifferences(tt2);
    }

    @Test
    public void diff() {

        Department d1 = Department.of("DA", "Department A");
        Department d2 = Department.of("DB", "Department B");
        Department d3 = Department.of("DC", "Department C");

        Section s1a = Section.of("SA")
                .addPeriod(RepeatingPeriod.of(TermClassifier.FALL_FIRST_QUARTER)
                        .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX))
                .addPeriod(OneTimePeriod.of(TermClassifier.FALL_SECOND_QUARTER)
                        .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX));
        Section s2a = Section.of("SB")
                .addPeriod(RepeatingPeriod.of(TermClassifier.FALL_FIRST_QUARTER)
                        .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX)
                        .setCampus("Campus").setRoom("Room"));
        Section s3a = Section.of("SC")
                .addPeriod(OneTimePeriod.of(TermClassifier.FALL_SECOND_QUARTER)
                        .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX));

        Term term = new Term(TermClassifier.FULL_YEAR, 2016);

        TimeTable tt1 = TimeTable.of(this.school, term);
        TimeTable tt2 = TimeTable.of(this.school, term);

        Section s1b = Section.of("SA")
                .addPeriod(RepeatingPeriod.of(TermClassifier.FALL_FIRST_QUARTER)
                        .setTime(DayOfWeek.THURSDAY, LocalTime.MIN, LocalTime.MAX))
                .addPeriod(OneTimePeriod.of(TermClassifier.FALL_SECOND_QUARTER)
                        .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX).setRoom("123"));
        Section s2b = Section.of("SB")
                .addPeriod(RepeatingPeriod.of(TermClassifier.FALL_FIRST_QUARTER)
                        .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX)
                        .setCampus("New Campus").setRoom("Room"));

        Course c2a = Course.of(this.school, TermClassifier.FULL_SUMMER, d2, "CB", "Course B");
        Course c3a = Course.of(this.school, TermClassifier.FALL, d3, "CC", "Course C")
                .addSection("A", s1a).addSection("B", s2a).addSection("C", s3a);
        tt1.addCourse(c2a).addCourse(c3a);

        Course c1b = Course.of(this.school, TermClassifier.FULL_SUMMER, d1, "CA", "Course A");
        Course c3b = Course.of(this.school, TermClassifier.FALL, d3, "CC", "Course C")
                .addSection("A", s1b).addSection("B", s2b);
        tt2.addCourse(c1b).addCourse(c3b);


        assertEquals(
                StructureChangeDelta.of(PropertyType.TIMETABLE, tt1)
                        .addRemoved(PropertyType.COURSE, c2a)
                        .addAdded(PropertyType.COURSE, c1b)
                        .addChange(c3a.findDifferences(c3b)),
                tt1.findDifferences(tt2));
    }

    @Test
    public void string() {
        ZonedDateTime zdt =
                ZonedDateTime.of(2016, 12, 12, 12, 12,12, 12, ZoneOffset.UTC);
        TimeTable tt = TimeTable.of(this.school, this.term, zdt);
        assertEquals(
                "[school: test_id, term: FALL 2016, " +
                        "departments: 0, courses: 0, last_update: " +
                        zdt.toString() + "]", tt.toString());
    }
}
