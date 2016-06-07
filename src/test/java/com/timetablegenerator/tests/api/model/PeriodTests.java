package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class PeriodTests {

    private RepeatingPeriod rp1;
    private RepeatingPeriod rp2;
    private OneTimePeriod otp1;
    private OneTimePeriod otp2;

    @Before
    public void setUp(){
        Settings.setIndentSize(4);
        TermClassifier t = TermClassifier.FALL;
        this.rp1 = RepeatingPeriod.of(t);
        this.rp2 = RepeatingPeriod.of(t);
        this.otp1 = OneTimePeriod.of(t);
        this.otp2 = OneTimePeriod.of(t);
    }

    @Test
    public void createRepeating() {
        for (TermClassifier t: TermClassifier.values()) {
            RepeatingPeriod rp = RepeatingPeriod.of(t);
            assertEquals(t, rp.getTerm());
        }
    }

    @Test
    public void createOneTime() {
        for (TermClassifier t: TermClassifier.values()) {
            OneTimePeriod otp = OneTimePeriod.of(t);
            assertEquals(t, otp.getTerm());
        }
    }

    @Test
    public void setTimeRepeating() {
        assertFalse(rp1.isScheduled());
        for (DayOfWeek dow: DayOfWeek.values()) {
            rp1.setTime(dow, LocalTime.MIN, LocalTime.MAX);
            assertEquals(dow, rp1.getDayOfWeek().orElse(null));
            assertEquals(LocalTime.MIN, rp1.getStartTime().orElse(null));
            assertEquals(LocalTime.MAX, rp1.getEndTime().orElse(null));
            assertTrue(rp1.isScheduled());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void setTimeRepeatingStartAfterEnd() {
        rp1.setTime(DayOfWeek.FRIDAY, LocalTime.MAX, LocalTime.MIN);
    }

    @Test
    public void setDateTimeOneTime() {
        assertFalse(otp1.isScheduled());
        otp1.setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);
        assertEquals(LocalDateTime.MIN, otp1.getStartDateTime().orElse(LocalDateTime.MAX));
        assertEquals(LocalDateTime.MAX, otp1.getEndDateTime().orElse(LocalDateTime.MIN));
        assertTrue(otp1.isScheduled());
    }

    @Test(expected = IllegalStateException.class)
    public void setDateTimeOneTimeStartAfterEnd() {
        otp1.setDateTimes(LocalDateTime.MAX, LocalDateTime.MIN);
    }

    @Test
    public void isOnline(){
        assertFalse(rp1.isOnline().isPresent());
        rp1.setOnline(true);
        assertTrue(rp1.isOnline().isPresent());
        assertTrue(rp1.isOnline().orElse(false));
        rp1.setOnline(false);
        assertTrue(rp1.isOnline().isPresent());
        assertFalse(rp1.isOnline().orElse(true));
    }

    @Test
    public void supervisors(){

        List<String> supervisors = TestUtils.getRandomStrings(10, 20);

        // Test singular addition.
        supervisors.forEach(rp1::addSupervisors);
        assertEquals(new HashSet<>(supervisors), rp1.getSupervisors());

        setUp();

        // Test collection addition.
        rp1.addSupervisors(supervisors);
        assertEquals(new HashSet<>(supervisors), rp1.getSupervisors());

        setUp();

        // Test array addition.
        rp1.addSupervisors(supervisors.toArray(new String[supervisors.size()]));
        assertEquals(new HashSet<>(supervisors), rp1.getSupervisors());

        List<String> supervisors2 = TestUtils.getRandomStrings(50, 120);
        rp1.addSupervisors(supervisors2);

        List<String> allSupervisors = new ArrayList<>(supervisors);
        allSupervisors.addAll(supervisors2);

        assertEquals(new HashSet<>(allSupervisors), rp1.getSupervisors());
    }

    @Test
    public void notes(){

        List<String> notes = TestUtils.getRandomStrings(10, 20);

        // Test singular addition.
        notes.forEach(rp1::addNotes);
        assertEquals(notes, rp1.getNotes());

        setUp();

        // Test collection addition.
        rp1.addNotes(notes);
        assertEquals(notes, rp1.getNotes());

        setUp();

        // Test array addition.
        rp1.addNotes(notes.toArray(new String[notes.size()]));
        assertEquals(notes, rp1.getNotes());

        List<String> notes2 = TestUtils.getRandomStrings(50, 120);
        rp1.addNotes(notes2);

        List<String> allNotes = new ArrayList<>(notes);
        allNotes.addAll(notes2);

        assertEquals(allNotes, rp1.getNotes());
    }

    @Test
    public void campus(){
        assertFalse(this.rp1.getCampus().isPresent());
        this.rp1.setCampus("Test Campus");
        assertTrue(this.rp1.getCampus().isPresent());
        assertEquals("Test Campus", this.rp1.getCampus().orElse("no campus set"));
    }

    @Test
    public void room(){
        assertFalse(this.rp1.getRoom().isPresent());
        this.rp1.setRoom("Test Room");
        assertTrue(this.rp1.getRoom().isPresent());
        assertEquals("Test Room", this.rp1.getRoom().orElse("no room set"));
    }

    @Test
    public void oneTimePeriodComparison(){

        // Try two equal terms
        assertEquals(0, otp1.compareTo(otp2));

        // Try one term greater than the other.
        otp1 = OneTimePeriod.of(TermClassifier.FALL);
        otp2 = OneTimePeriod.of(TermClassifier.SPRING);
        assertThat(otp1.compareTo(otp2), lessThan(0));
        assertThat(otp2.compareTo(otp1), greaterThan(0));

        // Compare different times.
        otp1 = OneTimePeriod.of(TermClassifier.FALL).setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);
        otp2 = OneTimePeriod.of(TermClassifier.FALL).setDateTimes(LocalDateTime.MAX, LocalDateTime.MAX);
        assertThat(otp1.compareTo(otp2), lessThan(0));
        assertThat(otp2.compareTo(otp1), greaterThan(0));

        // Compare set to non-set.
        otp1 = OneTimePeriod.of(TermClassifier.FALL);
        assertThat(otp1.compareTo(otp2), lessThan(0));
        assertThat(otp2.compareTo(otp1), greaterThan(0));
    }

    @Test
    public void repeatingPeriodComparison(){

        // Try two equal terms
        assertEquals(0, rp1.compareTo(rp2));

        // Try one term greater than the other.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL);
        rp2 = RepeatingPeriod.of(TermClassifier.SPRING);
        assertThat(rp1.compareTo(rp2), lessThan(0));
        assertThat(rp2.compareTo(rp1), greaterThan(0));

        // Compare different days of the week.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        rp2 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.SATURDAY, LocalTime.MIN, LocalTime.MAX);
        assertThat(rp1.compareTo(rp2), lessThan(0));
        assertThat(rp2.compareTo(rp1), greaterThan(0));

        // Compare set to unset day of week.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        rp2 = RepeatingPeriod.of(TermClassifier.FALL);
        assertThat(rp1.compareTo(rp2), greaterThan(0));
        assertThat(rp2.compareTo(rp1), lessThan(0));


        // Compare different start times.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        rp2 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MAX, LocalTime.MAX);
        assertThat(rp1.compareTo(rp2), lessThan(0));
        assertThat(rp2.compareTo(rp1), greaterThan(0));

        // Compare different end times.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MIN);
        rp2 = RepeatingPeriod.of(TermClassifier.FALL).setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        assertThat(rp1.compareTo(rp2), lessThan(0));
        assertThat(rp2.compareTo(rp1), greaterThan(0));
    }

    @Test
    public void periodEquality() {

        assertEquals(rp1, rp2);

        // Different term.
        rp2 = RepeatingPeriod.of(TermClassifier.SPRING);
        assertNotEquals(rp1, rp2);

        // Add a supervisor.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL);
        rp2.addSupervisors("Test");
        assertNotEquals(rp1, rp2);

        // Add a note.
        rp2 = RepeatingPeriod.of(TermClassifier.FALL);
        rp2.addNotes("123");
        assertNotEquals(rp1, rp2);

        // Set the room.
        rp2 = RepeatingPeriod.of(TermClassifier.FALL);
        rp2.setRoom("123");
        assertNotEquals(rp1, rp2);

        // Set online.
        rp2 = RepeatingPeriod.of(TermClassifier.FALL);
        rp2.setOnline(true);
        assertNotEquals(rp1, rp2);
        rp2.setOnline(false);
        assertNotEquals(rp1, rp2);
    }

    @Test
    public void repeatingPeriodEquality() {

        assertEquals(rp1, rp2);

        // Set time vs unset.
        rp2.setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        assertNotEquals(rp1, rp2);

        // Different day of the week.
        rp1.setTime(DayOfWeek.THURSDAY, LocalTime.MIN, LocalTime.MAX);
        assertNotEquals(rp1, rp2);

        // Different start time.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        rp2 = RepeatingPeriod.of(TermClassifier.FALL)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MAX, LocalTime.MAX);
        assertNotEquals(rp1, rp2);

        // Different end time.
        rp1 = RepeatingPeriod.of(TermClassifier.FALL)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MIN);
        rp2 = RepeatingPeriod.of(TermClassifier.FALL)
                .setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        assertNotEquals(rp1, rp2);
    }

    @Test
    public void oneTimePeriodEquality() {

        assertEquals(otp1, otp2);

        // Set time vs unset.
        otp2.setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);
        assertNotEquals(otp1, otp2);

        // Different start date time.
        otp1 = OneTimePeriod.of(TermClassifier.FALL)
                .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);
        otp2 = OneTimePeriod.of(TermClassifier.FALL)
                .setDateTimes(LocalDateTime.MAX, LocalDateTime.MAX);
        assertNotEquals(otp1, otp2);

        // Different end date time.
        otp1 = OneTimePeriod.of(TermClassifier.FALL)
                .setDateTimes(LocalDateTime.MIN, LocalDateTime.MIN);
        otp2 = OneTimePeriod.of(TermClassifier.FALL)
                .setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);
        assertNotEquals(otp1, otp2);
    }

    @Test
    public void repeatingPeriodString(){

        assertEquals("TBA TBA -> TBA [Term: FALL]", rp1.toString());

        // With a campus.
        rp1.setCampus("Test Campus");
        assertEquals("TBA TBA -> TBA [Term: FALL] (campus: Test Campus)", rp1.toString());

        // With a room.
        setUp();
        rp1.setRoom("Test Room");
        assertEquals("TBA TBA -> TBA [Term: FALL] (room: Test Room)", rp1.toString());

        // With a campus and room.
        rp1.setCampus("Test Campus");
        assertEquals("TBA TBA -> TBA [Term: FALL] " +
                "(campus: Test Campus, room: Test Room)", rp1.toString());

        // With a day, start time, end time, and day of week.
        setUp();
        rp1.setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX);
        assertEquals("FRIDAY 00:00 -> 23:59 [Term: FALL]", rp1.toString());

        // With campus and room.
        rp1.setRoom("Test Room");
        rp1.setCampus("Test Campus");
        assertEquals("FRIDAY 00:00 -> 23:59 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room)", rp1.toString());

        // With supervisors.
        rp1.addSupervisors("Billy", "Joe");
        assertEquals("FRIDAY 00:00 -> 23:59 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room) " +
                "[Instructors: 'Billy', 'Joe']", rp1.toString());

        // Set online.
        rp1.setOnline(true);
        assertEquals("FRIDAY 00:00 -> 23:59 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room) " +
                "(online) [Instructors: 'Billy', 'Joe']", rp1.toString());

        // As definite offline.
        rp1.setOnline(false);
        assertEquals("FRIDAY 00:00 -> 23:59 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room) " +
                "(not online) [Instructors: 'Billy', 'Joe']", rp1.toString());
    }

    @Test
    public void oneTimePeriodString(){

        assertEquals("TBA -> TBA [Term: FALL]", otp1.toString());

        // With a campus.
        otp1.setCampus("Test Campus");
        assertEquals("TBA -> TBA [Term: FALL] (campus: Test Campus)", otp1.toString());

        // With a room.
        setUp();
        otp1.setRoom("Test Room");
        assertEquals("TBA -> TBA [Term: FALL] (room: Test Room)", otp1.toString());

        // With a campus and room.
        otp1.setCampus("Test Campus");
        assertEquals("TBA -> TBA [Term: FALL] " +
                "(campus: Test Campus, room: Test Room)", otp1.toString());

        // With a day, start time, end time, and day of week.
        setUp();
        otp1.setDateTimes(LocalDateTime.of(2015, 11, 25, 11, 23),
                         LocalDateTime.of(2015, 11, 25, 12, 23));
        assertEquals("2015-11-25T11:23 -> 2015-11-25T12:23 [Term: FALL]", otp1.toString());

        // With campus and room.
        otp1.setRoom("Test Room");
        otp1.setCampus("Test Campus");
        assertEquals("2015-11-25T11:23 -> 2015-11-25T12:23 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room)", otp1.toString());

        // With supervisors.
        otp1.addSupervisors("Billy", "Joe");
        assertEquals("2015-11-25T11:23 -> 2015-11-25T12:23 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room) " +
                "[Instructors: 'Billy', 'Joe']", otp1.toString());

        // As online.
        otp1.setOnline(true);
        assertEquals("2015-11-25T11:23 -> 2015-11-25T12:23 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room) " +
                "(online) [Instructors: 'Billy', 'Joe']", otp1.toString());

        // As definite offline.
        otp1.setOnline(false);
        assertEquals("2015-11-25T11:23 -> 2015-11-25T12:23 [Term: FALL] " +
                "(campus: Test Campus, room: Test Room) " +
                "(not online) [Instructors: 'Billy', 'Joe']", otp1.toString());
    }

    @Test
    public void repeatingPeriodNoDeltaTest(){
        assertFalse(rp1.findDifferences(rp2).hasChanges());
    }

    @Test(expected = IllegalArgumentException.class)
    public void repeatingTimePeriodsIncomparableByTime(){
        rp1.setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX).findDifferences(rp2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void repeatingTimePeriodsIncomparableByTerm(){
        rp1.setTime(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX).findDifferences(rp2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void oneTimePeriodsIncomparableByTime(){
        otp1.setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX).findDifferences(otp2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void oneTimePeriodsIncomparableByTerm(){
        otp1.setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX).findDifferences(otp2);
    }

    @Test
    public void periodValuePropertyDelta(){

        // Room change
        rp1.setRoom("Test room");
        StructureChangeDelta expected = StructureChangeDelta.of(PropertyType.REPEATING_PERIOD,  rp2)
                .addAdded(PropertyType.ROOM, "Test room");
        StructureChangeDelta invertExpected = StructureChangeDelta.of(PropertyType.REPEATING_PERIOD,  rp1)
                .addRemoved(PropertyType.ROOM, "Test room");

        assertEquals(expected, rp2.findDifferences(rp1));
        assertEquals(invertExpected, rp1.findDifferences(rp2));

        // Campus change.
        rp1.setCampus("Test campus");
        rp2.setCampus("Test campus 1");
        expected.addValueIfChanged(PropertyType.CAMPUS, "Test campus 1", "Test campus");
        invertExpected.addValueIfChanged(PropertyType.CAMPUS, "Test campus", "Test campus 1");

        assertEquals(expected, rp2.findDifferences(rp1));
        assertEquals(invertExpected, rp1.findDifferences(rp2));

        // Online change.
        rp1.setOnline(false);
        expected.addAdded(PropertyType.IS_ONLINE, false);
        invertExpected.addRemoved(PropertyType.IS_ONLINE, false);

        assertEquals(expected, rp2.findDifferences(rp1));
        assertEquals(invertExpected, rp1.findDifferences(rp2));
    }

    @Test
    public void periodStructuralPropertySupervisorsDelta() {

        otp1.addSupervisors(Arrays.asList("test1", "test 2", "test 3"));
        otp2.addSupervisors("test 4", "test 2", "test 7", "test 8");

        StructureChangeDelta expected = StructureChangeDelta.of(PropertyType.ONE_TIME_PERIOD, otp1)
                .addRemoved(PropertyType.SUPERVISOR, "test1").addAdded(PropertyType.SUPERVISOR, "test 4")
                .addRemoved(PropertyType.SUPERVISOR, "test 3").addAdded(PropertyType.SUPERVISOR, "test 7")
                .addAdded(PropertyType.SUPERVISOR, "test 8");

        StructureChangeDelta invertExpected = StructureChangeDelta.of(PropertyType.ONE_TIME_PERIOD, otp2)
                .addRemoved(PropertyType.SUPERVISOR, "test 4").addAdded(PropertyType.SUPERVISOR, "test1")
                .addRemoved(PropertyType.SUPERVISOR, "test 7").addAdded(PropertyType.SUPERVISOR, "test 3")
                .addRemoved(PropertyType.SUPERVISOR, "test 8");

        assertEquals(expected, otp1.findDifferences(otp2));
        assertEquals(invertExpected, otp2.findDifferences(otp1));
    }

    @Test
    public void periodStructuralPropertyNotesDelta() {

        otp1.addNotes(Arrays.asList("test1", "test 2", "test 3"));
        otp2.addNotes("test 4", "test 2", "test 7", "test 8");

        StructureChangeDelta expected = StructureChangeDelta.of(PropertyType.ONE_TIME_PERIOD, otp1)
                .addRemoved(PropertyType.NOTE, "test1").addAdded(PropertyType.NOTE, "test 4")
                .addRemoved(PropertyType.NOTE, "test 3").addAdded(PropertyType.NOTE, "test 7")
                .addAdded(PropertyType.NOTE, "test 8");

        StructureChangeDelta invertExpected = StructureChangeDelta.of(PropertyType.ONE_TIME_PERIOD, otp2)
                .addRemoved(PropertyType.NOTE, "test 4").addAdded(PropertyType.NOTE, "test1")
                .addRemoved(PropertyType.NOTE, "test 7").addAdded(PropertyType.NOTE, "test 3")
                .addRemoved(PropertyType.NOTE, "test 8");

        assertEquals(expected, otp1.findDifferences(otp2));
        assertEquals(invertExpected, otp2.findDifferences(otp1));
    }
}