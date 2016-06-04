package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.model.*;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class TimeTableTests {

    private static String I;

    static {
        Settings.setIndentSize(4);
        I = Settings.getIndent();
    }

    private School school;
    private Term term = new Term(TermClassifier.FALL, 2016);
    private TimeTable tt = TimeTable.of(this.school, this.term);

    @Before
    public void setUp() {
        school = School.builder(TestUtils.getRandomString(20),
                                TestUtils.getRandomString(10))
                .withSection("Section Type A", "A")
                .withSection("Section Type B", "B")
                .withSection("Section Type C", "C")
                .withSection("Section Type D", "D").build();
    }

    @Test
    public void creation() {
        assertTrue(tt.getCourses().isEmpty());
        assertThat(tt.getLastUpdate().until(ZonedDateTime.now(), ChronoUnit.SECONDS),
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

        tt.addCourse(c);

    }
}
