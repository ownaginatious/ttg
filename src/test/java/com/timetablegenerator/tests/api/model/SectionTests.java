package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.model.Section;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TermDefinition;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class SectionTests {

    private static String I;

    static {
        Settings.setIndentSize(4);
        I = Settings.getIndent();
    }

    private Section s1, s2;

    private Term term_fall = TermDefinition.builder("fall", "Fall", 1).build().createForYear(2016);
    private Term term_fall_first_quarter = TermDefinition.builder("fall_fq", "Fall First Quarter", 2)
            .build().createForYear(2016);
    private Term term_fall_second_quarter = TermDefinition.builder("fall_sq", "Fall Second Quarter", 3)
            .build().createForYear(2016);

    @Before
    public void setUp() {
        Settings.setIndentSize(4);
        String sectionName = TestUtils.getRandomString(20);
        this.s1 = Section.of(sectionName);
        this.s2 = Section.of(sectionName);
    }

    @Test
    public void creation() {

        String sectionName = "some_section";

        Section section = Section.of(sectionName);
        assertEquals(section.getSectionId(), sectionName);
    }

    @Test
    public void enrollment() {

        // We do not know anything about enrollment or fullness yet.
        assertFalse(s1.getEnrollment().isPresent());
        assertFalse(s1.isFull().isPresent());

        for (int i = 0; i < 10; i++) {
            s1.setEnrollment(i);
            assertEquals(i, s1.getEnrollment().orElse(-1).intValue());
        }

        s1.setEnrollment(1);

        // No data on max fullness.
        assertFalse(s1.isFull().isPresent());
        assertFalse(s1.getMaxEnrollment().isPresent());

        // Lets set maximum enrollment.
        s1.setMaximumEnrollment(100);
        assertEquals(100, s1.getMaxEnrollment().orElse(-1).intValue());
        assertFalse(s1.isFull().orElse(true));

        // Max it out.
        s1.setEnrollment(100);
        assertTrue(s1.isFull().orElse(false));

        // Reduce by the minimal unit.
        s1.setEnrollment(99);
        assertFalse(s1.isFull().orElse(true));
    }

    @Test
    public void directSetEnrollmentFull() {

        assertFalse(s1.isFull().isPresent());
        s1.setFull(true);
        assertTrue(s1.isFull().isPresent());
        assertFalse(s1.getWaiting().isPresent());
        assertFalse(s1.getMaxWaiting().isPresent());
        s1.setFull(false);
        assertTrue(s1.isFull().isPresent());
        assertFalse(s1.isFull().orElse(false));
        s1.setMaximumEnrollment(99);
        s1.setEnrollment(23);
        s1.setFull(true);
        assertEquals(99, s1.getMaxEnrollment().orElse(-1).intValue());
        assertEquals(s1.getMaxEnrollment().orElse(-1), s1.getEnrollment().orElse(-1));
        s1.setFull(false);
        assertEquals(99, s1.getMaxEnrollment().orElse(-1).intValue());
        assertFalse(s1.getEnrollment().isPresent());

        // Reset the section.
        this.setUp();

        s1.setEnrollment(60);
        s1.setFull(true);
        assertEquals(60, s1.getEnrollment().orElse(-1).intValue());
        assertEquals(s1.getEnrollment().orElse(-1), s1.getMaxEnrollment().orElse(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void enrollmentOverflow() {

        s1.setMaximumEnrollment(100);
        s1.setEnrollment(101);
    }

    @Test(expected = IllegalArgumentException.class)
    public void enrollmentUnderflow() {

        s1.setMaximumEnrollment(100);
        s1.setEnrollment(99);
        s1.setMaximumEnrollment(98);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeMaxEnrollment() {
        s1.setMaximumEnrollment(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeEnrollment() {
        s1.setEnrollment(-1);
    }

    @Test
    public void waitingList() {

        // We do not know anything about waiting lists or waiting list fullness yet.
        assertFalse(s1.getWaiting().isPresent());
        assertFalse(s1.hasWaitingList().isPresent());

        // Toggle the presence of a waiting lists (e.g. there is one, no data though)
        s1.setWaitingList(true);
        assertTrue(s1.hasWaitingList().isPresent());
        assertTrue(s1.hasWaitingList().orElse(false));

        s1.setWaitingList(false);
        assertTrue(s1.hasWaitingList().isPresent());
        assertFalse(s1.hasWaitingList().orElse(false));

        for (int i = 0; i < 10; i++) {
            s1.setWaiting(i);
            assertEquals(i, s1.getWaiting().orElse(-1).intValue());
        }

        s1.setWaiting(1);

        // No data on max fullness.
        assertFalse(s1.isFull().isPresent());
        assertFalse(s1.getMaxEnrollment().isPresent());

        // Let's1 set maximum waiting.
        s1.setMaximumWaiting(100);
        assertEquals(100, s1.getMaxWaiting().orElse(-1).intValue());

        // Max it out.
        s1.setWaiting(100);
        assertEquals(s1.getMaxWaiting(), s1.getWaiting());

        // Reduce by the minimal unit.
        s1.setWaiting(99);
        assertThat(s1.getMaxWaiting().orElse(-1), greaterThan(s1.getWaiting().orElse(-1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void waitingListOverflow() {

        s1.setMaximumWaiting(100);
        s1.setWaiting(101);
    }

    @Test(expected = IllegalArgumentException.class)
    public void waitingListUnderflow() {

        s1.setMaximumWaiting(100);
        s1.setWaiting(99);
        s1.setMaximumWaiting(98);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeWaiting() {
        s1.setMaximumWaiting(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeMaxWaiting() {
        s1.setWaiting(-1);
    }

    @Test
    public void notes(){

        List<String> notes = TestUtils.getRandomStrings(50, 120);

        // Test singular addition.
        notes.forEach(s1::addNotes);
        assertEquals(notes, s1.getNotes());

        setUp();

        // Test collection addition.
        s1.addNotes(notes);
        assertEquals(notes, s1.getNotes());

        setUp();

        // Test array addition.
        s1.addNotes(notes.toArray(new String[notes.size()]));
        assertEquals(notes, s1.getNotes());

        List<String> notes2 = TestUtils.getRandomStrings(50, 120);
        s1.addNotes(notes2);

        List<String> allNotes = new ArrayList<>(notes);
        allNotes.addAll(notes2);

        assertEquals(allNotes, s1.getNotes());
    }

    @Test
    public void online(){
        assertFalse(s1.isOnline().isPresent());
        s1.setOnline(true);
        assertTrue(s1.isOnline().isPresent());
        assertTrue(s1.isOnline().orElse(false));
        s1.setOnline(false);
        assertTrue(s1.isOnline().isPresent());
        assertFalse(s1.isOnline().orElse(true));
    }

    @Test
    public void cancelled(){
        assertFalse(s1.isCancelled().isPresent());
        s1.setCancelled(true);
        assertTrue(s1.isCancelled().isPresent());
        assertTrue(s1.isCancelled().orElse(false));
        s1.setCancelled(false);
        assertTrue(s1.isCancelled().isPresent());
        assertFalse(s1.isCancelled().orElse(true));
    }

    @Test
    public void alternating(){
        assertFalse(s1.isAlternating().isPresent());
        s1.setAlternating(true);
        assertTrue(s1.isAlternating().isPresent());
        assertTrue(s1.isAlternating().orElse(false));
        s1.setAlternating(false);
        assertTrue(s1.isAlternating().isPresent());
        assertFalse(s1.isAlternating().orElse(true));
    }

    @Test
    public void serialNumber(){
        String serial = TestUtils.getRandomString(10);
        assertFalse(s1.getSerialNumber().isPresent());
        s1.setSerialNumber(serial);
        assertEquals(serial, s1.getSerialNumber().orElse(""));
    }

    @Test
    public void periods() {

        RepeatingPeriod p1 = RepeatingPeriod.of(this.term_fall_first_quarter)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        RepeatingPeriod p2 = RepeatingPeriod.of(this.term_fall_second_quarter)
                .setTime(DayOfWeek.TUESDAY, LocalTime.MIN, LocalTime.MAX);
        OneTimePeriod p3 = OneTimePeriod.of(this.term_fall_second_quarter)
                .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);

        s1.addPeriod(p1);
        s1.addPeriod(p1);
        s1.addPeriod(p2);
        s1.addPeriod(p3);

        assertEquals(new TreeSet<>(Collections.singletonList(p3)), s1.getOneTimePeriods());
        assertEquals(new TreeSet<>(Arrays.asList(p1, p2)), s1.getRepeatingPeriods());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deltaUnrelatedSections(){

        Section s = s1;
        setUp();

        s.findDifferences(s1);
    }

    @Test
    public void sectionValuePropertyDelta(){

        // Serial number change.
        s1.setSerialNumber("123");
        s2.setSerialNumber("456");

        StructureDelta expected = StructureDelta.of(PropertyType.SECTION, s1)
                .addValueIfChanged(PropertyType.SERIAL_NUMBER, "123", "456");
        StructureDelta invertExpected = StructureDelta.of(PropertyType.SECTION, s2)
                .addValueIfChanged(PropertyType.SERIAL_NUMBER, "456", "123");

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));

        // Waiting list values change.
        s2.setWaiting(5).setMaximumWaiting(10);

        expected.addAdded(PropertyType.NUM_WAITING, 5)
                           .addAdded(PropertyType.MAX_WAITING, 10).addAdded(PropertyType.WAITING_LIST, true);
        invertExpected.addRemoved(PropertyType.NUM_WAITING, 5)
                      .addRemoved(PropertyType.MAX_WAITING, 10).addRemoved(PropertyType.WAITING_LIST, true);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));

        // Enrollment values change.
        s2.setEnrollment(5).setMaximumEnrollment(10);

        expected.addAdded(PropertyType.NUM_ENROLLED, 5)
                .addAdded(PropertyType.MAX_ENROLLED, 10).addAdded(PropertyType.IS_FULL, false);
        invertExpected.addRemoved(PropertyType.NUM_ENROLLED, 5)
                .addRemoved(PropertyType.MAX_ENROLLED, 10).addRemoved(PropertyType.IS_FULL, false);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));

        // Cancellation change.
        s2.setCancelled(true);
        s1.setCancelled(false);

        expected.addValueIfChanged(PropertyType.IS_CANCELLED, false, true);
        invertExpected.addValueIfChanged(PropertyType.IS_CANCELLED, true, false);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));

        // Online change.
        s2.setOnline(true);
        s1.setOnline(false);

        expected.addValueIfChanged(PropertyType.IS_ONLINE, false, true);
        invertExpected.addValueIfChanged(PropertyType.IS_ONLINE, true, false);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));

        // Alternating change.
        s2.setAlternating(true);
        s1.setAlternating(false);

        expected.addValueIfChanged(PropertyType.IS_ALTERNATING, false, true);
        invertExpected.addValueIfChanged(PropertyType.IS_ALTERNATING, true, false);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));
    }

    @Test
    public void sectionStructuralPropertyNotesDelta() {

        s1.addNotes("test1", "test 2", "test 3");
        s2.addNotes("test 4", "test 2", "test 7", "test 8");

        StructureDelta expected = StructureDelta.of(PropertyType.SECTION, s1)
                .addRemoved(PropertyType.NOTE, "test1").addAdded(PropertyType.NOTE, "test 4")
                .addRemoved(PropertyType.NOTE, "test 3").addAdded(PropertyType.NOTE, "test 7")
                .addAdded(PropertyType.NOTE, "test 8");

        StructureDelta invertExpected = StructureDelta.of(PropertyType.SECTION, s2)
                .addRemoved(PropertyType.NOTE, "test 4").addAdded(PropertyType.NOTE, "test1")
                .addRemoved(PropertyType.NOTE, "test 7").addAdded(PropertyType.NOTE, "test 3")
                .addRemoved(PropertyType.NOTE, "test 8");

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));
    }

    @Test
    public void sectionStructuralPropertyPeriodsDelta() {

        RepeatingPeriod p1 = RepeatingPeriod.of(this.term_fall_first_quarter).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        RepeatingPeriod p2 = RepeatingPeriod.of(this.term_fall_second_quarter).setTime(DayOfWeek.TUESDAY, LocalTime.MIN, LocalTime.MAX);
        OneTimePeriod p3 = OneTimePeriod.of(this.term_fall_second_quarter).setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX)
                .setRoom("My Room");

        OneTimePeriod p4 = OneTimePeriod.of(this.term_fall_second_quarter).setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX)
                .addSupervisors("Test Supervisor 1").addSupervisors("Test Supervisor 2");

        RepeatingPeriod p5 = RepeatingPeriod.of(this.term_fall_second_quarter).setTime(DayOfWeek.TUESDAY, LocalTime.MIN, LocalTime.MAX)
                .setCampus("Test Campus").addNotes("Note 1", "Note 2", "Note 3");

        OneTimePeriod p6 = OneTimePeriod.of(this.term_fall).setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX)
                .addSupervisors("Test Supervisor 1");

        s2.addPeriod(p1);
        s2.addPeriod(p2);
        s2.addPeriod(p3);
        s2.addPeriod(p6);

        s1.addPeriod(p4);
        s1.addPeriod(p5);

        StructureDelta expected = StructureDelta.of(PropertyType.SECTION, s1)
                .addAdded(PropertyType.REPEATING_PERIOD, p1)
                .addAdded(PropertyType.ONE_TIME_PERIOD, p6)
                .addSubstructureChange(StructureDelta.of(PropertyType.REPEATING_PERIOD, p5)
                        .addRemoved(PropertyType.CAMPUS, "Test Campus")
                        .addRemoved(PropertyType.NOTE, "Note 1")
                        .addRemoved(PropertyType.NOTE, "Note 2")
                        .addRemoved(PropertyType.NOTE, "Note 3"))
                .addSubstructureChange(StructureDelta.of(PropertyType.ONE_TIME_PERIOD, p4)
                        .addRemoved(PropertyType.SUPERVISOR, "Test Supervisor 1")
                        .addRemoved(PropertyType.SUPERVISOR, "Test Supervisor 2")
                        .addAdded(PropertyType.ROOM, "My Room"));

        StructureDelta invertExpected = StructureDelta.of(PropertyType.SECTION, s2)
                .addRemoved(PropertyType.REPEATING_PERIOD, p1)
                .addRemoved(PropertyType.ONE_TIME_PERIOD, p6)
                .addSubstructureChange(StructureDelta.of(PropertyType.REPEATING_PERIOD, p2)
                        .addAdded(PropertyType.CAMPUS, "Test Campus")
                        .addAdded(PropertyType.NOTE, "Note 1")
                        .addAdded(PropertyType.NOTE, "Note 2")
                        .addAdded(PropertyType.NOTE, "Note 3"))
                .addSubstructureChange(StructureDelta.of(PropertyType.ONE_TIME_PERIOD, p3)
                        .addAdded(PropertyType.SUPERVISOR, "Test Supervisor 1")
                        .addAdded(PropertyType.SUPERVISOR, "Test Supervisor 2")
                        .addRemoved(PropertyType.ROOM, "My Room"));

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));
    }

    @Test
    public void emptySectionString() {
        assertEquals(s1.getSectionId(), s1.toString());
    }

    @Test
    public void sectionWithSerialString(){
        s1.setSerialNumber("Test Serial");
        assertEquals(s1.getSectionId() + " {Test Serial}", s1.toString());
    }

    @Test
    public void sectionCancelledString(){
        String header = s1.getSectionId() + " {Test Serial}";
        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        assertEquals(header + " [CANCELLED]", s1.toString());
        s1.setCancelled(false);
        assertEquals(header, s1.toString());
    }

    @Test
    public void sectionOnlineString(){
        String header = s1.getSectionId() + " {Test Serial} [CANCELLED]";
        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        s1.setOnline(true);
        assertEquals(header + " [ONLINE]", s1.toString());
        s1.setOnline(false);
        assertEquals(header, s1.toString());
    }

    @Test
    public void sectionAlternating(){
        String header = s1.getSectionId() + " {Test Serial} [CANCELLED] [ONLINE]";
        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        s1.setOnline(true);
        s1.setAlternating(true);
        assertEquals(header + " [ALTERNATES]", s1.toString());
        s1.setAlternating(false);
        assertEquals(header, s1.toString());
    }

    @Test
    public void sectionFullString(){
        String header = s1.getSectionId() + " {Test Serial} [CANCELLED]";
        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        s1.setFull(true);
        assertEquals(header + " [FULL]", s1.toString());
        s1.setFull(false);
        assertEquals(header + " [AVAILABLE]", s1.toString());
    }

    @Test
    public void sectionEnrollmentString(){
        String header = s1.getSectionId() + " {Test Serial} [CANCELLED] [ONLINE] [ALTERNATES]";
        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        s1.setEnrollment(3);
        s1.setOnline(true);
        s1.setAlternating(true);
        assertEquals(header + " [enrolled: 3/?]", s1.toString());
        s1.setMaximumEnrollment(4);
        assertEquals(header + " [AVAILABLE] [enrolled: 3/4]", s1.toString());
        s1.setMaximumEnrollment(3);
        assertEquals(header + " [FULL] [enrolled: 3/3]", s1.toString());
    }

    @Test
    public void sectionWaitingString(){
        String header = s1.getSectionId()
                + " {Test Serial} [CANCELLED] [ONLINE] [ALTERNATES] [enrolled: 3/?]";
        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        s1.setOnline(true);
        s1.setAlternating(true);
        s1.setWaiting(50);
        s1.setEnrollment(3);
        assertEquals(header + " [waiting: 50/?]", s1.toString());
        s1.setMaximumWaiting(100);
        assertEquals(header + " [waiting: 50/100]", s1.toString());
        s1.setWaiting(100);
        assertEquals(header + " [waiting: 100/100]", s1.toString());
    }

    @Test
    public void sectionPeriodsString(){
        String header = s1.getSectionId()
                + " {Test Serial} [CANCELLED] [ONLINE] [ALTERNATES] [enrolled: 3/?] [waiting: 100/100]";
        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        s1.setOnline(true);
        s1.setAlternating(true);
        s1.setEnrollment(3);
        s1.setWaiting(100);
        s1.setMaximumWaiting(100);

        // Add one-time and unique periods.
        OneTimePeriod p1 = OneTimePeriod.of(this.term_fall_first_quarter)
                .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX)
                .addNotes("Test 1", "Test 2", "Test 3");
        RepeatingPeriod p2 = RepeatingPeriod.of(this.term_fall_first_quarter)
                .setTime(DayOfWeek.MONDAY, LocalTime.MIN, LocalTime.MAX)
                .addNotes("Test 1", "Test 2", "Test 3").addSupervisors("A Test", "B Test");
        RepeatingPeriod p3 = RepeatingPeriod.of(this.term_fall_first_quarter)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX)
                .addNotes("Test 1", "Test 2", "Test 3").addSupervisors("A Test");

        s1.addPeriod(p1).addPeriod(p2).addPeriod(p3);

        String expected = header + "\n\n" + I + "Repeating periods:\n\n"
                + StringUtilities.indent(2, p2.toString()) + "\n\n"
                + StringUtilities.indent(2, p3.toString()) + "\n"
                + "\n" + I + "One time periods:\n\n"
                + StringUtilities.indent(2, p1.toString());

        assertEquals(expected, s1.toString());
    }

    @Test
    public void sectionNotesString(){

        s1.setSerialNumber("Test Serial");
        s1.setCancelled(true);
        s1.setOnline(true);
        s1.setAlternating(true);
        s1.setEnrollment(3);
        s1.setWaiting(100);
        s1.setMaximumWaiting(100);

        // Add one-time and unique periods.
        OneTimePeriod p1 = OneTimePeriod.of(this.term_fall_first_quarter)
                .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX)
                .addNotes("Test 1", "Test 2", "Test 3");
        RepeatingPeriod p2 = RepeatingPeriod.of(this.term_fall_first_quarter)
                .setTime(DayOfWeek.MONDAY, LocalTime.MIN, LocalTime.MAX)
                .addNotes("Test 1", "Test 2", "Test 3").addSupervisors("A Test", "B Test");
        RepeatingPeriod p3 = RepeatingPeriod.of(this.term_fall_first_quarter)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX)
                .addNotes("Test 1", "Test 2", "Test 3").addSupervisors("A Test");

        String expected = s1.getSectionId() +
                " {Test Serial} [CANCELLED] [ONLINE] [ALTERNATES] [enrolled: 3/?] [waiting: 100/100]\n\n"
                + I + "Notes:\n\n"
                + I + I + "This is my note\n"
                + I + I + "This is my note\n"
                + I + I + "    with a line break!\n\n"
                + I + "Repeating periods:\n\n"
                + StringUtilities.indent(2, p2.toString()) + "\n\n"
                + StringUtilities.indent(2, p3.toString())
                + "\n\n" + I + "One time periods:\n\n"
                + StringUtilities.indent(2, p1.toString());

        s1.addPeriod(p1).addPeriod(p2).addPeriod(p3);
        s1.addNotes("This is my note", "This is my note\n    with a line break!");

        assertEquals(expected, s1.toString());
    }

    @Test
    public void sectionEquality(){

        // Same section ID.
        Section s1 = Section.of("testing");
        Section s2 = Section.of("testing");
        assertEquals(s1, s2);

        // Differing section IDs.
        s2 = Section.of("testing 2");
        assertNotEquals(s1, s2);
        s2 = Section.of("testing");

        // Toggle full
        s1.setFull(true);
        assertNotEquals(s1, s2);
        s1.setFull(false);
        assertNotEquals(s1, s2);
        s2.setFull(false);
        assertEquals(s1, s2);

        // Toggle online
        s1.setOnline(true);
        assertNotEquals(s1, s2);
        s1.setOnline(false);
        assertNotEquals(s1, s2);
        s2.setOnline(false);
        assertEquals(s1, s2);

        // Toggle alternating
        s1.setAlternating(true);
        assertNotEquals(s1, s2);
        s1.setAlternating(false);
        assertNotEquals(s1, s2);
        s2.setAlternating(false);
        assertEquals(s1, s2);

        // Toggle waiting list
        s1.setWaitingList(true);
        assertNotEquals(s1, s2);
        s1.setWaitingList(false);
        assertNotEquals(s1, s2);
        s2.setWaitingList(false);
        assertEquals(s1, s2);

        // Differing serial numbers
        s1.setSerialNumber("test");
        assertNotEquals(s1, s2);
        s2.setSerialNumber("test");
        assertEquals(s1, s2);

        // Change enrollment
        s1.setEnrollment(1);
        assertNotEquals(s1, s2);
        s2.setEnrollment(1);
        assertEquals(s1, s2);

        // Change max enrollment
        s1.setMaximumEnrollment(1);
        assertNotEquals(s1, s2);
        s2.setMaximumEnrollment(1);
        assertEquals(s1, s2);

        // Change waiting
        s1.setWaiting(1);
        assertNotEquals(s1, s2);
        s2.setWaiting(1);
        assertEquals(s1, s2);

        // Change max waiting
        s1.setMaximumWaiting(1);
        assertNotEquals(s1, s2);
        s2.setMaximumWaiting(1);
        assertEquals(s1, s2);

        // Modify one-time periods
        OneTimePeriod p1 = OneTimePeriod.of(this.term_fall_first_quarter);
        s1.addPeriod(p1);
        assertNotEquals(s1, s2);
        s2.addPeriod(p1);
        assertEquals(s1, s2);

        // Modify repeating periods
        RepeatingPeriod rp1 = RepeatingPeriod.of(this.term_fall)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        s1.addPeriod(rp1);
        assertNotEquals(s1, s2);
        s2.addPeriod(rp1);
        assertEquals(s1, s2);
    }
}