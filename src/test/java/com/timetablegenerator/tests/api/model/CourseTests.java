package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.model.*;
import static org.junit.Assert.*;

import com.timetablegenerator.model.range.DateTimeRange;
import com.timetablegenerator.model.range.DayTimeRange;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CourseTests {

    private static String I;

    static {
        Settings.setIndentSize(4);
        I = Settings.getIndent();
    }

    private School school;
    private Term term = TermDefinition.builder("fall", "Fall", 1).build().createForYear(2016);
    private Term term_fq = TermDefinition.builder("fall_fq", "Fall First Quarter", 1).build().createForYear(2016);
    private Term term_sq = TermDefinition.builder("fall_sq", "Fall Second Quarter", 1).build().createForYear(2016);
    private Department department =
            Department.of(TestUtils.getRandomString(5),
                    TestUtils.getRandomString(20));

    private Course getRandomCourse(){

        String courseCode = TestUtils.getRandomString(5);
        String courseName = TestUtils.getRandomString(20);

        return Course.of(this.school, this.term, this.department,
                courseCode, courseName);
    }

    @Before
    public void setUp() {
        this.school = School.builder(TestUtils.getRandomString(10),
                TestUtils.getRandomString(20))
                .withSection("A", "Section Type A")
                .withSection("B", "Section Type B")
                .withSection("C", "Section Type C")
                .withSection("D", "Section Type D").build();
    }

    @Test
    public void creation() {

        String courseCode = TestUtils.getRandomString(5);
        String courseName = TestUtils.getRandomString(20);

        Course c = Course.of(this.school, this.term, this.department,
                             courseCode, courseName);

        assertEquals(this.school, c.getSchool());
        assertEquals(this.term, c.getTerm());
        assertEquals(this.department, c.getDepartment());
        assertEquals(courseCode, c.getCode());
        assertEquals(courseName, c.getName());
        assertEquals(this.department.getCode() + courseCode + this.term.getTermDefinition().getCode(),
                     c.getUniqueId());
    }

    @Test
    public void credits() {
        double credits = 2.0;
        Course c = getRandomCourse();
        assertFalse(c.getCredits().isPresent());
        c.setCredits(credits);
        assertTrue(c.getCredits().isPresent());
        assertEquals(credits, c.getCredits().orElse(-1.0), 0.00001);
    }

    @Test
    public void description() {
        String description = TestUtils.getRandomString(1000);
        Course c = getRandomCourse();
        assertFalse(c.getDescription().isPresent());
        c.setDescription(description);
        assertTrue(c.getDescription().isPresent());
        assertEquals(description, c.getDescription().orElse(null));
    }

    @Test
    public void prerequisite() {

        Course c1 = getRandomCourse();
        Course c2 = getRandomCourse();
        Course c3 = getRandomCourse();

        c1.addPrerequisite(c2);
        Map<String, Course> expected = new HashMap<>();
        expected.put(c2.getUniqueId(), c2);
        assertEquals(expected, c1.getPrerequisites());
        c1.addPrerequisite(c3);
        expected.put(c3.getUniqueId(), c3);
        assertEquals(expected, c1.getPrerequisites());
        c1.addPrerequisite(c2);
        assertEquals(expected, c1.getPrerequisites());
    }

    @Test(expected = IllegalArgumentException.class)
    public void selfPrerequisite(){
        Course c1 = getRandomCourse();
        c1.addPrerequisite(c1);
    }

    @Test
    public void antirequisite() {

        Course c1 = getRandomCourse();
        Course c2 = getRandomCourse();
        Course c3 = getRandomCourse();

        c1.addAntirequisite(c2);
        Map<String, Course> expected = new HashMap<>();
        expected.put(c2.getUniqueId(), c2);
        assertEquals(expected, c1.getAntirequisites());
        c1.addAntirequisite(c3);
        expected.put(c3.getUniqueId(), c3);
        assertEquals(expected, c1.getAntirequisites());
        c1.addAntirequisite(c2);
        assertEquals(expected, c1.getAntirequisites());
    }

    @Test(expected = IllegalArgumentException.class)
    public void selfAntirequisite(){
        Course c1 = getRandomCourse();
        c1.addAntirequisite(c1);
    }

    @Test
    public void crosslisting() {

        Course c1 = getRandomCourse();
        Course c2 = getRandomCourse();
        Course c3 = getRandomCourse();

        c1.addCrossListing(c2);
        Map<String, Course> expected = new HashMap<>();
        expected.put(c2.getUniqueId(), c2);
        assertEquals(expected, c1.getCrossListings());
        c1.addCrossListing(c3);
        expected.put(c3.getUniqueId(), c3);
        assertEquals(expected, c1.getCrossListings());
        c1.addCrossListing(c2);
        assertEquals(expected, c1.getCrossListings());
    }

    @Test(expected = IllegalArgumentException.class)
    public void selfCrosslisting(){
        Course c1 = getRandomCourse();
        c1.addCrossListing(c1);
    }

    @Test
    public void corequisite() {

        Course c1 = getRandomCourse();
        Course c2 = getRandomCourse();
        Course c3 = getRandomCourse();

        c1.addCorequesite(c2);
        Map<String, Course> expected = new HashMap<>();
        expected.put(c2.getUniqueId(), c2);
        assertEquals(expected, c1.getCorequisites());
        c1.addCorequesite(c3);
        expected.put(c3.getUniqueId(), c3);
        assertEquals(expected, c1.getCorequisites());
        c1.addCorequesite(c2);
        assertEquals(expected, c1.getCorequisites());
    }

    @Test(expected = IllegalArgumentException.class)
    public void selfCorequisite(){
        Course c1 = getRandomCourse();
        c1.addCorequesite(c1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multiListedRelation(){
        Course c1 = getRandomCourse();
        Course c2 = getRandomCourse();
        c1.addPrerequisite(c2);
        c1.addCorequesite(c2);
    }

    @Test
    public void notes(){

        Course c = getRandomCourse();
        List<String> notes = TestUtils.getRandomStrings(50, 120);

        // Test singular addition.
        notes.forEach(c::addNotes);
        assertEquals(notes, c.getNotes());

        // Reset
        c = getRandomCourse();

        // Test collection addition.
        c.addNotes(notes);
        assertEquals(notes, c.getNotes());

        // Reset
        c = getRandomCourse();

        // Test array addition.
        c.addNotes(notes.toArray(new String[notes.size()]));
        assertEquals(notes, c.getNotes());

        List<String> notes2 = TestUtils.getRandomStrings(50, 120);
        c.addNotes(notes2);

        List<String> allNotes = new ArrayList<>(notes);
        allNotes.addAll(notes2);

        assertEquals(allNotes, c.getNotes());
    }

    @Test
    public void sectionTypes() {
        Course c = getRandomCourse();
        Section s1 = Section.of("A01");
        Section s2 = Section.of("B01");
        Section s3 = Section.of("B02");

        c.addSection("A", s1);
        assertEquals(new HashSet<>(Collections.singleton("A")), c.getSectionTypes());
        c.addSection("B", s2);
        assertEquals(new HashSet<>(Arrays.asList("A", "B")), c.getSectionTypes());
        c.addSection("B", s3);
        assertEquals(new HashSet<>(Arrays.asList("A", "B")), c.getSectionTypes());

        SectionType st = SectionType.of(school, "B");
        st.addSection(s3);
        st.addSection(s2);

        assertEquals(st, c.getSectionType("B").orElse(null));
    }

    @Test
    public void compare() {

        Department d1 =
                Department.of("DEP A", "Test");
        Department d2 =
                Department.of("DEP B", "Test 2");

        Course c1 = Course.of(this.school, this.term, d1,
                "ABC", "Test1");
        Course c2 = Course.of(this.school, this.term, d1,
                "AAD", "Test2");
        Course c3 = Course.of(this.school, this.term, d2,
                "RQI", "Test3");
        Course c4 = Course.of(this.school, this.term, d2,
                "RQI", "Test4");

        List<Course> courses = Arrays.asList(c1, c2, c3, c4, c4);
        Collections.sort(courses);

        assertEquals(Arrays.asList(c2, c1, c3, c4, c4), courses);
    }

    @Test
    public void unchangedDelta() {

        String courseCode = TestUtils.getRandomString(4);
        String courseName = TestUtils.getRandomString(20);

        Course c1 = Course.of(this.school, this.term, this.department,
                courseCode, courseName);
        Course c2 = Course.of(this.school, this.term, this.department,
                courseCode, courseName);

        assertFalse(c1.findDifferences(c2).hasChanges());
    }

    @Test(expected = IllegalArgumentException.class)
    public void incomparableDelta() {
        getRandomCourse().findDifferences(getRandomCourse());
    }

    @Test
    public void valueDelta() {

        String courseCode = TestUtils.getRandomString(4);
        String courseName = TestUtils.getRandomString(20);

        Course c1 = Course.of(this.school, this.term, this.department,
                courseCode, courseName).setCredits(1.0);
        Course c2 = Course.of(this.school, this.term, this.department,
                courseCode, courseName).setCredits(2.0);

        assertEquals(
                StructureDelta.of(PropertyType.COURSE, c1)
                        .addValueIfChanged(PropertyType.CREDITS, 1.0, 2.0),
                c1.findDifferences(c2)
        );

        String description = TestUtils.getRandomString(400);
        c2.setCredits(1.0).setDescription(description);

        assertEquals(
                StructureDelta.of(PropertyType.COURSE, c1)
                        .addAdded(PropertyType.DESCRIPTION, description),
                c1.findDifferences(c2)
        );

        c1.setDescription(description);
        Course c3 = getRandomCourse();
        Course c4 = getRandomCourse();
        Course c5 = getRandomCourse();
        c1.addAntirequisite(c3);
        c2.addPrerequisite(c4);
        c2.addCrossListing(c5);

        assertEquals(
                StructureDelta.of(PropertyType.COURSE, c1)
                        .addRemoved(PropertyType.ANTI_REQUISITE, c3)
                        .addAdded(PropertyType.PRE_REQUISITE, c4)
                        .addAdded(PropertyType.CROSS_LISTING, c5),
                c1.findDifferences(c2)
        );

        c2.addAntirequisite(c3);
        c1.addPrerequisite(c4);
        c1.addCrossListing(c5);

        c1.addNotes("It's", "a", "test!");
        c2.addNotes("a", "testing");

        assertEquals(
                StructureDelta.of(PropertyType.COURSE, c1)
                        .addAdded(PropertyType.NOTE, "testing")
                        .addRemoved(PropertyType.NOTE, "test!")
                        .addRemoved(PropertyType.NOTE, "It's"),
                c1.findDifferences(c2)
        );
    }

    @Test
    public void childStructuralDelta() {

        String courseCode = TestUtils.getRandomString(4);
        String courseName = TestUtils.getRandomString(20);

        Course c1 = Course.of(this.school, this.term, this.department,
                courseCode, courseName).setCredits(1.0);
        Course c2 = Course.of(this.school, this.term, this.department,
                courseCode, courseName).setCredits(2.0);

        Section s0 = Section.of("A1");
        Section s1 = Section.of("A2");
        Section s2 = Section.of("A3");

        Section s3 = Section.of("B1");
        Section s4 = Section.of("B2");
        Section s5 = Section.of("B3");

        c1.addSection("A", s0).addSection("A", s1);
        c2.addSection("A", s0).addSection("A", s2)
                .addSection("B", s3).addSection("B", s4).addSection("B", s5);

        assertEquals(
                StructureDelta.of(PropertyType.COURSE, c1)
                        .addAdded(PropertyType.SECTION_TYPE, c2.getSectionType("B").orElse(null))
                        .addSubstructureChange(c1.getSectionType("A").orElse(null)
                                .findDifferences(c2.getSectionType("A").orElse(null))
                        ).addValueIfChanged(PropertyType.CREDITS,
                            c1.getCredits().orElse(null), c2.getCredits().orElse(null)
                        ),
                c1.findDifferences(c2)
        );
    }

    @Test
    public void string(){

        Course c = getRandomCourse();
        Course c1 = getRandomCourse();
        Course c2 = getRandomCourse();
        Course c3 = getRandomCourse();
        Course c4 = getRandomCourse();
        Course c5 = getRandomCourse();
        Course c6 = getRandomCourse();

        c.addAntirequisite(c1).addPrerequisite(c2)
                .addPrerequisite(c5).addCrossListing(c3)
                .addCorequesite(c4).addCorequesite(c6);

        Section s1 = Section.of("A01")
                .addPeriod(OneTimePeriod.of(this.term_fq)
                        .setDateTimeRange(DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX))
                        .setCampus("Campus B").setRoom("987")
                        .addNotes("Some note", "Some note 2"));
        Section s2 = Section.of("A02")
                .addPeriod(RepeatingPeriod.of(this.term_sq)
                        .setDayTimeRange(DayTimeRange.of(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX))
                        .setCampus("Campus B").setRoom("997"));
        c.addSection("A", s1).addSection("A", s2);
        Section s3 = Section.of("B01")
                .addPeriod(RepeatingPeriod.of(this.term_fq)
                        .setDayTimeRange(DayTimeRange.of(DayOfWeek.THURSDAY, LocalTime.MIN, LocalTime.MAX))
                        .setCampus("Campus A").setRoom("123"));
        c.addSection("B", s3);
        c.addNotes("Note 1", "Note 2", "Note 3\n\tNote 3 continuation...");
        assertEquals(
                String.format(
                        "* Course: %s [%s]\n" +
                        "* Department: %s\n" +
                        "* Term: %s\n" +
                        "* Cross-listings: %s\n" +
                        "* Pre-requisites: %s\n" +
                        "* Anti-requisites: %s\n" +
                        "* Co-requisites: %s\n" +
                        "* Notes:\n\n"
                                + I + "Note 1\n"
                                + I + "Note 2\n"
                                + I + "Note 3\n"
                                + I + "\tNote 3 continuation...\n\n" +
                        "* Sections:\n\n%s\n\n%s",
                        c.getName(), c.getCode(),
                        c.getDepartment().toString(),
                        c.getTerm().toString(),
                        c3.getUniqueId(),
                        Stream.of(c2.getUniqueId(), c5.getUniqueId()).sorted()
                                .collect(Collectors.joining(", ")),
                        c1.getUniqueId(),
                        Stream.of(c4.getUniqueId(), c6.getUniqueId()).sorted()
                                .collect(Collectors.joining(", ")),
                        StringUtilities.indent(1, c.getSectionType("A").orElse(null).toString()),
                        StringUtilities.indent(1, c.getSectionType("B").orElse(null).toString())),
                c.toString());
    }

    @Test
    public void stringWithoutSections(){
        Course c = getRandomCourse().setCredits(2.0);
        assertEquals(
            String.format(
                "* Course: %s [%s]\n" +
                "* Department: %s\n" +
                "* Term: %s\n" +
                "* Credits: %.1f\n" +
                "* Sections:\n\n" + I + "NONE",
                c.getName(), c.getCode(),
                c.getDepartment().toString(),
                c.getTerm().toString(), 2.0), c.toString());
    }

    @Test
    public void equality(){

        String courseCode = TestUtils.getRandomString(4);
        String courseName = TestUtils.getRandomString(20);

        Course c1 = Course.of(this.school, this.term, this.department,
                courseCode, courseName);
        Course c2 = Course.of(this.school, this.term, this.department,
                courseCode, courseName);
        Course c3 = getRandomCourse();

        assertFalse(c1.equals(c3));
        assertFalse(c2.equals(c3));

        assertTrue(c1.equals(c2));

        c1.setCredits(2.0);
        assertFalse(c1.equals(c2));
        c2.setCredits(2.0);
        assertTrue(c1.equals(c2));

    }
}
