package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.model.Section;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class SectionTests {

    private Section s;

    @Before
    public void setUp() {
        this.s = Section.fromSectionId(TestUtils.getRandomString(20));
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
        assertFalse(s.getEnrollment().isPresent());
        assertFalse(s.isFull().isPresent());

        for (int i = 0; i < 10; i++) {
            s.setEnrollment(i);
            assertEquals(i, s.getEnrollment().orElse(-1).intValue());
        }

        s.setEnrollment(1);

        // No data on max fullness.
        assertFalse(s.isFull().isPresent());
        assertFalse(s.getMaxEnrollment().isPresent());

        // Lets set maximum enrollment.
        s.setMaximumEnrollment(100);
        assertEquals(100, s.getMaxEnrollment().orElse(-1).intValue());
        assertFalse(s.isFull().orElse(true));

        // Max it out.
        s.setEnrollment(100);
        assertTrue(s.isFull().orElse(false));

        // Reduce by the minimal unit.
        s.setEnrollment(99);
        assertFalse(s.isFull().orElse(true));
    }

    @Test
    public void directSetEnrollmentFull() {

        assertFalse(s.isFull().isPresent());
        s.setFull(true);
        assertTrue(s.isFull().isPresent());
        assertFalse(s.getWaiting().isPresent());
        assertFalse(s.getMaxWaiting().isPresent());
        s.setFull(false);
        assertTrue(s.isFull().isPresent());
        assertFalse(s.isFull().orElse(false));
        s.setMaximumEnrollment(99);
        s.setEnrollment(23);
        s.setFull(true);
        assertEquals(99, s.getMaxEnrollment().orElse(-1).intValue());
        assertEquals(s.getMaxEnrollment().orElse(-1), s.getEnrollment().orElse(-1));
        s.setFull(false);
        assertEquals(99, s.getMaxEnrollment().orElse(-1).intValue());
        assertFalse(s.getEnrollment().isPresent());

        // Reset the section.
        this.setUp();

        s.setEnrollment(60);
        s.setFull(true);
        assertEquals(60, s.getEnrollment().orElse(-1).intValue());
        assertEquals(s.getEnrollment().orElse(-1), s.getMaxEnrollment().orElse(-1));
    }

    @Test(expected = IllegalStateException.class)
    public void enrollmentOverflow() {

        s.setMaximumEnrollment(100);
        s.setEnrollment(101);
    }

    @Test(expected = IllegalStateException.class)
    public void enrollmentUnderflow() {

        s.setMaximumEnrollment(100);
        s.setEnrollment(99);
        s.setMaximumEnrollment(98);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeMaxEnrollment() {
        s.setMaximumEnrollment(-1);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeEnrollment() {
        s.setEnrollment(-1);
    }

    @Test
    public void waitingList() {

        // We do not know anything about waiting lists or waiting list fullness yet.
        assertFalse(s.getWaiting().isPresent());
        assertFalse(s.hasWaitingList().isPresent());

        // Toggle the presence of a waiting lists (e.g. there is one, no data though)
        s.setWaitingList(true);
        assertTrue(s.hasWaitingList().isPresent());
        assertTrue(s.hasWaitingList().orElse(false));

        s.setWaitingList(false);
        assertTrue(s.hasWaitingList().isPresent());
        assertFalse(s.hasWaitingList().orElse(false));

        for (int i = 0; i < 10; i++) {
            s.setWaiting(i);
            assertEquals(i, s.getWaiting().orElse(-1).intValue());
        }

        s.setWaiting(1);

        // No data on max fullness.
        assertFalse(s.isFull().isPresent());
        assertFalse(s.getMaxEnrollment().isPresent());

        // Let's set maximum waiting.
        s.setMaximumWaiting(100);
        assertEquals(100, s.getMaxWaiting().orElse(-1).intValue());

        // Max it out.
        s.setWaiting(100);
        assertEquals(s.getMaxWaiting(), s.getWaiting());

        // Reduce by the minimal unit.
        s.setWaiting(99);
        assertThat(s.getMaxWaiting().orElse(-1), greaterThan(s.getWaiting().orElse(-1)));
    }

    @Test(expected = IllegalStateException.class)
    public void waitingListOverflow() {

        s.setMaximumWaiting(100);
        s.setWaiting(101);
    }

    @Test(expected = IllegalStateException.class)
    public void waitingListUnderflow() {

        s.setMaximumWaiting(100);
        s.setWaiting(99);
        s.setMaximumWaiting(98);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeWaiting() {
        s.setMaximumWaiting(-1);
    }

    @Test(expected = IllegalStateException.class)
    public void negativeMaxWaiting() {
        s.setWaiting(-1);
    }

    @Test
    public void notes(){

        List<String> notes = TestUtils.getRandomStrings(50, 120);

        // Test singular addition.
        notes.forEach(s::addNotes);
        assertEquals(notes, s.getNotes());

        setUp();

        // Test collection addition.
        s.addNotes(notes);
        assertEquals(notes, s.getNotes());

        setUp();

        // Test array addition.
        s.addNotes(notes.toArray(new String[notes.size()]));
        assertEquals(notes, s.getNotes());

        List<String> notes2 = TestUtils.getRandomStrings(50, 120);
        s.addNotes(notes2);

        List<String> allNotes = new ArrayList<>(notes);
        allNotes.addAll(notes2);

        assertEquals(allNotes, s.getNotes());
    }

    @Test
    public void online(){
        assertFalse(s.isOnline().isPresent());
        s.setOnline(true);
        assertTrue(s.isOnline().isPresent());
        assertTrue(s.isOnline().orElse(false));
        s.setOnline(false);
        assertTrue(s.isOnline().isPresent());
        assertFalse(s.isOnline().orElse(true));
    }

    @Test
    public void cancelled(){
        assertFalse(s.isCancelled().isPresent());
        s.setCancelled(true);
        assertTrue(s.isCancelled().isPresent());
        assertTrue(s.isCancelled().orElse(false));
        s.setCancelled(false);
        assertTrue(s.isCancelled().isPresent());
        assertFalse(s.isCancelled().orElse(true));
    }

    @Test
    public void alternating(){
        assertFalse(s.isAlternating().isPresent());
        s.setAlternating(true);
        assertTrue(s.isAlternating().isPresent());
        assertTrue(s.isAlternating().orElse(false));
        s.setAlternating(false);
        assertTrue(s.isAlternating().isPresent());
        assertFalse(s.isAlternating().orElse(true));
    }

    @Test
    public void serialNumber(){
        String serial = TestUtils.getRandomString(10);
        assertFalse(s.getSerialNumber().isPresent());
        s.setSerialNumber(serial);
        assertEquals(serial, s.getSerialNumber().orElse(""));
    }
}