package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.Section;
import com.timetablegenerator.model.TermClassifier;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.Period;
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

    private Section s1, s2;

    @Before
    public void setUp() {
        String sectionName = TestUtils.getRandomString(20);
        this.s1 = Section.fromSectionId(sectionName);
        this.s2 = Section.fromSectionId(sectionName);
    }

    @Test
    public void creation() {

        String sectionName = "some_section";

        Section section = Section.fromSectionId(sectionName);
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

    @Test(expected = IllegalStateException.class)
    public void enrollmentOverflow() {

        s1.setMaximumEnrollment(100);
        s1.setEnrollment(101);
    }

    @Test(expected = IllegalStateException.class)
    public void enrollmentUnderflow() {

        s1.setMaximumEnrollment(100);
        s1.setEnrollment(99);
        s1.setMaximumEnrollment(98);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeMaxEnrollment() {
        s1.setMaximumEnrollment(-1);
    }

    @Test(expected = IllegalStateException.class)
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

    @Test(expected = IllegalStateException.class)
    public void waitingListOverflow() {

        s1.setMaximumWaiting(100);
        s1.setWaiting(101);
    }

    @Test(expected = IllegalStateException.class)
    public void waitingListUnderflow() {

        s1.setMaximumWaiting(100);
        s1.setWaiting(99);
        s1.setMaximumWaiting(98);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeWaiting() {
        s1.setMaximumWaiting(-1);
    }

    @Test(expected = IllegalStateException.class)
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

        Period p1 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        Period p2 = RepeatingPeriod.of(TermClassifier.SPRING).setTime(DayOfWeek.TUESDAY, LocalTime.MIN, LocalTime.MAX);
        Period p3 = OneTimePeriod.of(TermClassifier.SPRING).setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);

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

        StructureChangeDelta expected = StructureChangeDelta.of(PropertyType.SECTION, s1)
                .addIfChanged(PropertyType.SERIAL_NUMBER, "123", "456");
        StructureChangeDelta invertExpected = StructureChangeDelta.of(PropertyType.SECTION, s2)
                .addIfChanged(PropertyType.SERIAL_NUMBER, "456", "123");

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

        expected.addIfChanged(PropertyType.IS_CANCELLED, false, true);
        invertExpected.addIfChanged(PropertyType.IS_CANCELLED, true, false);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));

        // Online change.
        s2.setOnline(true);
        s1.setOnline(false);

        expected.addIfChanged(PropertyType.IS_ONLINE, false, true);
        invertExpected.addIfChanged(PropertyType.IS_ONLINE, true, false);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));

        // Alternating change.
        s2.setAlternating(true);
        s1.setAlternating(false);

        expected.addIfChanged(PropertyType.IS_ALTERNATING, false, true);
        invertExpected.addIfChanged(PropertyType.IS_ALTERNATING, true, false);

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));
    }

    @Test
    public void sectionStructuralPropertyNotesDelta() {

        s1.addNotes("test1", "test 2", "test 3");
        s2.addNotes("test 4", "test 2", "test 7", "test 8");

        StructureChangeDelta expected = StructureChangeDelta.of(PropertyType.SECTION, s1)
                .addRemoved(PropertyType.NOTE, "test1").addAdded(PropertyType.NOTE, "test 4")
                .addRemoved(PropertyType.NOTE, "test 3").addAdded(PropertyType.NOTE, "test 7")
                .addAdded(PropertyType.NOTE, "test 8");

        StructureChangeDelta invertExpected = StructureChangeDelta.of(PropertyType.SECTION, s2)
                .addRemoved(PropertyType.NOTE, "test 4").addAdded(PropertyType.NOTE, "test1")
                .addRemoved(PropertyType.NOTE, "test 7").addAdded(PropertyType.NOTE, "test 3")
                .addRemoved(PropertyType.NOTE, "test 8");

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));
    }

    @Test
    public void sectionStructuralPropertyPeriodsDelta() {

        Period p1 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        Period p2 = RepeatingPeriod.of(TermClassifier.SPRING).setTime(DayOfWeek.TUESDAY, LocalTime.MIN, LocalTime.MAX);
        Period p3 = OneTimePeriod.of(TermClassifier.SPRING).setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX)
                .setRoom("My Room");

        Period p4 = OneTimePeriod.of(TermClassifier.SPRING).setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX)
                .addSupervisors("Test Supervisor 1").addSupervisors("Test Supervisor 2");

        Period p5 = RepeatingPeriod.of(TermClassifier.SPRING).setTime(DayOfWeek.TUESDAY, LocalTime.MIN, LocalTime.MAX)
                .setCampus("Test Campus").addNotes("Note 1", "Note 2", "Note 3");

        s2.addPeriod(p1);
        s2.addPeriod(p2);
        s2.addPeriod(p3);

        s1.addPeriod(p4);
        s1.addPeriod(p5);

        StructureChangeDelta expected = StructureChangeDelta.of(PropertyType.SECTION, s1)
                .addAdded(PropertyType.REPEATING_PERIOD, p1)
                .addChange(StructureChangeDelta.of(PropertyType.REPEATING_PERIOD, (RepeatingPeriod) p5)
                        .addRemoved(PropertyType.CAMPUS, "Test Campus")
                        .addRemoved(PropertyType.NOTE, "Note 1")
                        .addRemoved(PropertyType.NOTE, "Note 2")
                        .addRemoved(PropertyType.NOTE, "Note 3"))
                .addChange(StructureChangeDelta.of(PropertyType.ONE_TIME_PERIOD, (OneTimePeriod) p4)
                        .addRemoved(PropertyType.SUPERVISOR, "Test Supervisor 1")
                        .addRemoved(PropertyType.SUPERVISOR, "Test Supervisor 2")
                        .addAdded(PropertyType.ROOM, "My Room"));

        StructureChangeDelta invertExpected = StructureChangeDelta.of(PropertyType.SECTION, s2)
                .addRemoved(PropertyType.REPEATING_PERIOD, p1)
                .addChange(StructureChangeDelta.of(PropertyType.REPEATING_PERIOD, (RepeatingPeriod) p2)
                        .addAdded(PropertyType.CAMPUS, "Test Campus")
                        .addAdded(PropertyType.NOTE, "Note 1")
                        .addAdded(PropertyType.NOTE, "Note 2")
                        .addAdded(PropertyType.NOTE, "Note 3"))
                .addChange(StructureChangeDelta.of(PropertyType.ONE_TIME_PERIOD, (OneTimePeriod) p3)
                        .addAdded(PropertyType.SUPERVISOR, "Test Supervisor 1")
                        .addAdded(PropertyType.SUPERVISOR, "Test Supervisor 2")
                        .addRemoved(PropertyType.ROOM, "My Room"));

        assertEquals(expected, s1.findDifferences(s2));
        assertEquals(invertExpected, s2.findDifferences(s1));
    }
}