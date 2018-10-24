package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.model.range.DateRange;
import com.timetablegenerator.model.range.DateTimeRange;
import com.timetablegenerator.model.range.DayTimeRange;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RangeTests {

    private final LocalTime MAX_TIME = LocalTime.MAX.truncatedTo(ChronoUnit.MINUTES);
    private final LocalDateTime MAX_DATETIME = LocalDateTime.MAX.truncatedTo(ChronoUnit.MINUTES);

    /**
     * Tests for DateRange
     */

    @Test
    public void dateRangeCreation() {
        DateRange dr = DateRange.of(LocalDate.MIN, LocalDate.MAX);
        assertEquals(dr.getStartDate(), LocalDate.MIN);
        assertEquals(dr.getEndDate(), LocalDate.MAX);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badDateRange() {
        DateRange.of(LocalDate.MAX, LocalDate.MIN);
    }

    @Test
    public void dateRangeCompare() {
        DateRange dr1 = DateRange.of(LocalDate.MIN, LocalDate.MAX);
        DateRange dr2 = DateRange.of(LocalDate.MIN.plus(1, ChronoUnit.DAYS), LocalDate.MAX);
        DateRange dr3 = DateRange.of(LocalDate.MIN, LocalDate.MAX.minus(1, ChronoUnit.DAYS));

        assertEquals(dr1.compareTo(dr1), 0);
        assertThat(dr2.compareTo(dr1), greaterThan(0));
        assertThat(dr3.compareTo(dr1), lessThan(0));
    }

    @Test
    public void dateRangeString() {
        DateRange dr = DateRange.of(LocalDate.of(2015, 8, 9), LocalDate.of(2016, 9, 12));
        assertEquals(dr.toString(), "2015-08-09 -> 2016-09-12");
    }

    /**
     * Tests for DateTimeRange
     */

    @Test
    public void dateTimeRangeCreation() {
        DateTimeRange dr = DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX);
        assertEquals(dr.getStartDateTime(), LocalDateTime.MIN);
        assertEquals(dr.getEndDateTime(), MAX_DATETIME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badDateTimeRange() {
        DateTimeRange.of(LocalDateTime.MAX, LocalDateTime.MIN);
    }

    @Test
    public void dateTimeRangeCompare() {
        DateTimeRange dtr1 = DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX);
        DateTimeRange dtr2 = DateTimeRange.of(LocalDateTime.MIN.plus(1, ChronoUnit.DAYS), LocalDateTime.MAX);
        DateTimeRange dtr3 = DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX.minus(1, ChronoUnit.DAYS));

        assertEquals(dtr1.compareTo(dtr1), 0);
        assertThat(dtr2.compareTo(dtr1), greaterThan(0));
        assertThat(dtr3.compareTo(dtr1), lessThan(0));
    }

    @Test
    public void dateTimeRangeString() {
        DateTimeRange dr = DateTimeRange.of(LocalDateTime.of(2015, 8, 9, 11, 25), LocalDateTime.of(2016, 9, 12, 23, 42));
        assertEquals(dr.toString(), "2015-08-09T11:25 -> 2016-09-12T23:42");
    }

    /**
     * Tests for DayTimeRange
     */

    @Test
    public void dayTimeRangeCreation() {
        DayTimeRange dr = DayTimeRange.of(DayOfWeek.MONDAY, LocalTime.MIN, LocalTime.MAX);
        assertEquals(dr.getStartTime(), LocalTime.MIN);
        assertEquals(dr.getEndTime(), MAX_TIME);
        assertEquals(dr.getDayOfWeek(), DayOfWeek.MONDAY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badDayTimeRange() {
        DayTimeRange.of(DayOfWeek.MONDAY, LocalTime.MAX, LocalTime.MIN);
    }

    @Test
    public void dayTimeRangeCompare() {
        DayTimeRange dtr1 = DayTimeRange.of(DayOfWeek.MONDAY, LocalTime.MIN, LocalTime.MAX);
        DayTimeRange dtr2 = DayTimeRange.of(DayOfWeek.MONDAY, LocalTime.MIN.plus(1, ChronoUnit.HOURS), LocalTime.MAX);
        DayTimeRange dtr3 = DayTimeRange.of(DayOfWeek.MONDAY, LocalTime.MIN, LocalTime.MAX.minus(1, ChronoUnit.HOURS));
        DayTimeRange dtr4 = DayTimeRange.of(DayOfWeek.TUESDAY, LocalTime.MIN, LocalTime.MAX);

        assertEquals(dtr1.compareTo(dtr1), 0);
        assertThat(dtr2.compareTo(dtr1), greaterThan(0));
        assertThat(dtr3.compareTo(dtr1), lessThan(0));
        assertThat(dtr1.compareTo(dtr4), lessThan(0));
        assertThat(dtr4.compareTo(dtr1), greaterThan(0));
    }

    @Test
    public void dayTimeRangeString() {
        DayTimeRange dr = DayTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(11, 25), LocalTime.of(23, 42));
        assertEquals("MONDAY 11:25 -> 23:42", dr.toString());
    }
}