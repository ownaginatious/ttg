package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.exceptions.TermScopeException;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.*;
import com.timetablegenerator.model.range.DateTimeRange;
import com.timetablegenerator.model.range.DayTimeRange;
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

    private Term term = TermDefinition.builder("fall", "Fall", 1)
            .withSubterm(TermDefinition.builder("fall_fq", "Fall First Quarter", 1).build())
            .withSubterm(TermDefinition.builder("fall_sq", "Fall Second Quarter", 1).build())
            .build().createForYear(2016);
    private Term term_fq = term.getSubterm("fall_fq");
    private Term term_sq = term.getSubterm("fall_sq");

    private School school;
    private TimeTable tt;

    @Before
    public void setUp() {
        school = School.builder("test_id", "test_name")
                .withSection("A", "Section Type A")
                .withSection("B", "Section Type B")
                .withSection("C", "Section Type C")
                .withSection("D", "Section Type D").build();
        tt = TimeTable.of(this.school, this.term);
    }

    @Test
    public void creation() {
        assertTrue(tt.getCourses().isEmpty());
        assertThat(tt.getLastUpdate().until(ZonedDateTime.now(ZoneOffset.UTC), ChronoUnit.SECONDS),
                lessThan(1L));
        assertEquals(this.term, tt.getTerm());
        assertEquals(this.school, tt.getSchool());
        ZonedDateTime zdt = ZonedDateTime.now();
        tt = TimeTable.of(this.school, this.term, zdt);
        assertEquals(tt.getLastUpdate(), zdt);
    }

    @Test
    public void courseAddition() {
        Department d = Department.of("ABC", "Testing 123");
        Course c = Course.of(this.school, this.term, d, "Code", "Name");
        Course c2 = Course.of(this.school, this.term, d, "Code2", "Name");
        tt.addCourse(c).addCourse(c2);
        assertEquals(
                new HashSet<>(Arrays.asList(c, c2)),
                new HashSet<>(tt.getCourses()));

        assertEquals(c, tt.getCourse(c.getUniqueId()).orElse(null));
        assertEquals(c2, tt.getCourse(c2.getUniqueId()).orElse(null));

        assertFalse(tt.getCourse("no_such_id").isPresent());
    }

    @Test(expected = TermScopeException.class)
    public void outOfScopeCourseAddition() {
        tt = TimeTable.of(this.school, this.term_fq);
        Department d = Department.of("ABC", "Testing 123");
        Course c = Course.of(this.school, this.term, d, "Code", "Name");
        tt.addCourse(c);
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateCourseAddition() {
        Department d = Department.of("ABC", "Testing 123");
        Course c = Course.of(this.school, this.term, d, "Code", "Name");
        Course c2 = Course.of(this.school, this.term, d, "Code", "Name");
        tt.addCourse(c).addCourse(c2);
    }

    @Test
    public void compare() {

        // By school.
        TimeTable tta = TimeTable.of(School.builder("a", "a").build(), this.term);
        TimeTable ttb = TimeTable.of(School.builder("b", "b").build(), this.term);
        assertThat(tta.compareTo(ttb), lessThan(0));

        // By term
        TimeTable tt1 = TimeTable.of(this.school, this.term_fq);
        TimeTable tt2 = TimeTable.of(this.school, this.term_sq);
        TimeTable tt3 = TimeTable.of(this.school, this.term);
        List<TimeTable> timetables = Arrays.asList(tt1, tt2, tt3);
        Collections.sort(timetables);
        assertEquals(Arrays.asList(tt3, tt1, tt2), timetables);

        // By else
        assertEquals(0, tta.compareTo(tta));
    }

    @Test
    public void noDiff() {
        Department d = Department.of("ABC", "Testing 123");
        Course c = Course.of(this.school, this.term, d, "Code", "Name");
        Course c2 = Course.of(this.school, this.term, d, "Code", "Name");
        TimeTable tt1 = TimeTable.of(this.school, this.term).addCourse(c);
        TimeTable tt2 = TimeTable.of(this.school, this.term).addCourse(c2);
        assertFalse(tt1.findDifferences(tt2).hasChanges());
    }

    @Test(expected = IllegalArgumentException.class)
    public void differentTermDiff() {
        TimeTable tt1 = TimeTable.of(this.school, this.term_fq);
        TimeTable tt2 = TimeTable.of(this.school, this.term_sq);
        tt1.findDifferences(tt2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void differentSchoolDiff() {
        School s1 = School.builder("A", "School A").build();
        School s2 = School.builder("B", "School B").build();
        TimeTable tt1 = TimeTable.of(s1, this.term);
        TimeTable tt2 = TimeTable.of(s2, this.term);
        tt1.findDifferences(tt2);
    }

    @Test
    public void diff() {

        Department d1 = Department.of("DA", "Department A");
        Department d2 = Department.of("DB", "Department B");
        Department d3 = Department.of("DC", "Department C");

        Section s1a = Section.of(this.term_sq, "SA")
                .addPeriod(RepeatingPeriod.of(this.term_sq)
                        .setDayTimeRange(DayTimeRange.of(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX)))
                .addPeriod(OneTimePeriod.of(this.term_sq)
                        .setDateTimeRange(DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX)));
        Section s2a = Section.of(this.term_sq, "SB")
                .addPeriod(RepeatingPeriod.of(this.term_sq)
                        .setDayTimeRange(DayTimeRange.of(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX))
                        .setCampus("Campus").setRoom("Room"));
        Section s3a = Section.of(this.term_sq, "SC")
                .addPeriod(OneTimePeriod.of(this.term_sq)
                        .setDateTimeRange(DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX)));

        TimeTable tt1 = TimeTable.of(this.school, this.term);
        TimeTable tt2 = TimeTable.of(this.school, this.term);

        Section s1b = Section.of(this.term_sq, "SA")
                .addPeriod(RepeatingPeriod.of(this.term_sq)
                        .setDayTimeRange(DayTimeRange.of(DayOfWeek.THURSDAY, LocalTime.MIN, LocalTime.MAX)))
                .addPeriod(OneTimePeriod.of(this.term_sq)
                        .setDateTimeRange(DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX)).setRoom("123"));
        Section s2b = Section.of(this.term_sq, "SB")
                .addPeriod(RepeatingPeriod.of(this.term_sq)
                        .setDayTimeRange(DayTimeRange.of(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX))
                        .setCampus("New Campus").setRoom("Room"));

        Course c2a = Course.of(this.school, this.term_fq, d2, "CB", "Course B");
        Course c3a = Course.of(this.school, this.term_sq, d3, "CC", "Course C")
                .addSection("A", s1a).addSection("B", s2a).addSection("C", s3a);
        tt1.addCourse(c2a).addCourse(c3a);

        Course c1b = Course.of(this.school, this.term_fq, d1, "CA", "Course A");
        Course c3b = Course.of(this.school, this.term_sq, d3, "CC", "Course C")
                .addSection("A", s1b).addSection("B", s2b);
        tt2.addCourse(c1b).addCourse(c3b);


        assertEquals(
                StructureDelta.of(PropertyType.TIMETABLE, tt1)
                        .addRemoved(PropertyType.COURSE, c2a)
                        .addAdded(PropertyType.COURSE, c1b)
                        .addSubstructureChange(c3a.findDifferences(c3b)),
                tt1.findDifferences(tt2));
    }

    @Test
    public void string() {
        ZonedDateTime zdt =
                ZonedDateTime.of(2016, 12, 12, 12, 12,12, 12, ZoneOffset.UTC);
        TimeTable tt = TimeTable.of(this.school, this.term, zdt);
        assertEquals(
                "[school: test_id, term: Fall (fall) 2016, " +
                        "departments: 0, courses: 0, last_update: " +
                        zdt.toString() + "]", tt.toString());
    }
}
