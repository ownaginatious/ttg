package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.model.TermClassifier;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.Period;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class PeriodTests {

    private RepeatingPeriod rp;
    private OneTimePeriod otp;

    @Before
    public void setUp(){
        TermClassifier t = TermClassifier.FALL;
        this.rp = RepeatingPeriod.of(t);
        this.otp = OneTimePeriod.of(t);
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
        for (DayOfWeek dow: DayOfWeek.values()) {
            rp.setTime(dow, LocalTime.MIN, LocalTime.MAX);
            assertEquals(dow, rp.getDayOfWeek());
            assertEquals(LocalTime.MIN, rp.getStartTime());
            assertEquals(LocalTime.MAX, rp.getEndTime());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void setTimeRepeatingStartAfterEnd() {
        rp.setTime(DayOfWeek.FRIDAY, LocalTime.MAX, LocalTime.MIN);
    }

    @Test
    public void setDateTimeOneTime() {
        otp.setDateTimes(LocalDateTime.MIN, LocalDateTime.MAX);
        assertEquals(LocalDateTime.MIN, otp.getStartDateTime());
        assertEquals(LocalDateTime.MAX, otp.getEndDateTime());
    }

    @Test(expected = IllegalStateException.class)
    public void setDateTimeOneTimeStartAfterEnd() {
        otp.setDateTimes(LocalDateTime.MAX, LocalDateTime.MIN);
    }

    @Test
    public void isScheduledRepeating() {

    }

    @Test
    public void isOnline(){
        assertFalse(rp.isOnline().isPresent());
        rp.setOnline(true);
        assertTrue(rp.isOnline().isPresent());
        assertTrue(rp.isOnline().orElse(false));
        rp.setOnline(false);
        assertTrue(rp.isOnline().isPresent());
        assertFalse(rp.isOnline().orElse(true));
    }

    @Test
    public void supervisorsRepeating(){
        this.supervisors(rp);
    }

    @Test
    public void supervisorsOneTime(){
        this.supervisors(otp);
    }

    private void supervisors(Period p){

        List<String> supervisors = TestUtils.getRandomStrings(10, 20);

        // Test singular addition.
        supervisors.forEach(p::addSupervisors);
        assertEquals(new HashSet<>(supervisors), p.getSupervisors());

        setUp();

        // Test collection addition.
        p.addSupervisors(supervisors);
        assertEquals(new HashSet<>(supervisors), p.getSupervisors());

        setUp();

        // Test array addition.
        p.addSupervisors(supervisors.toArray(new String[supervisors.size()]));
        assertEquals(new HashSet<>(supervisors), p.getSupervisors());

        List<String> supervisors2 = TestUtils.getRandomStrings(50, 120);
        p.addSupervisors(supervisors2);

        List<String> allSupervisors = new ArrayList<>(supervisors);
        allSupervisors.addAll(supervisors2);

        assertEquals(new HashSet<>(allSupervisors), p.getSupervisors());
    }

    @Test
    public void addNotesRepeating(){
        this.notes(rp);
    }

    @Test
    public void addNotesOneTime(){
        this.notes(otp);
    }

    private void notes(Period p){

        List<String> notes = TestUtils.getRandomStrings(10, 20);

        // Test singular addition.
        notes.forEach(p::addNotes);
        assertEquals(notes, p.getNotes());

        setUp();

        // Test collection addition.
        p.addNotes(notes);
        assertEquals(notes, p.getNotes());

        setUp();

        // Test array addition.
        p.addNotes(notes.toArray(new String[notes.size()]));
        assertEquals(notes, p.getNotes());

        List<String> notes2 = TestUtils.getRandomStrings(50, 120);
        p.addNotes(notes2);

        List<String> allNotes = new ArrayList<>(notes);
        allNotes.addAll(notes2);

        assertEquals(allNotes, p.getNotes());
    }
}