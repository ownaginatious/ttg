package com.timetablegenerator.tests.api.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timetablegenerator.model.range.DateRange;
import com.timetablegenerator.model.range.DateTimeRange;
import com.timetablegenerator.model.range.DayTimeRange;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class RangeTests {

    private ObjectMapper objectMapper;

    @Before
    public void setUp(){
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Tests for DateRange
     */

    @Test
    public void dateRangeToJson() throws IOException {
        DateRange dateRange = DateRange.of(LocalDate.of(2015, 8, 9), LocalDate.of(2016, 9, 12));
        String dump = this.objectMapper.writeValueAsString(dateRange);
        assertEquals(dump, "{\"startDate\":\"2015-08-09\",\"endDate\":\"2016-09-12\"}");
    }

    @Test
    public void dateRangeFromJson() throws IOException {
        String rawJson = "{\"startDate\":\"2015-08-09\",\"endDate\":\"2016-09-12\"}";
        DateRange actual = this.objectMapper.readValue(rawJson, DateRange.class);
        DateRange expected = DateRange.of(LocalDate.of(2015, 8, 9), LocalDate.of(2016, 9, 12));
        assertEquals(actual, expected);
    }

    /**
     * Tests for DateTimeRange
     */

    @Test
    public void dateTimeRangeToJson() throws IOException {
        DateTimeRange dateTimeRange = DateTimeRange.of(LocalDateTime.of(2015, 8, 9, 11, 25), LocalDateTime.of(2016, 9, 12, 23, 42));
        String dump = this.objectMapper.writeValueAsString(dateTimeRange);
        assertEquals(dump, "{\"startDateTime\":\"2015-08-09T11:25\",\"endDateTime\":\"2016-09-12T23:42\"}");
    }

    @Test
    public void dateTimeRangeFromJson() throws IOException {
        String rawJson = "{\"startDateTime\":\"2015-08-09T11:25\",\"endDateTime\":\"2016-09-12T23:42\"}";
        DateTimeRange actual = this.objectMapper.readValue(rawJson, DateTimeRange.class);
        DateTimeRange expected = DateTimeRange.of(LocalDateTime.of(2015, 8, 9, 11, 25), LocalDateTime.of(2016, 9, 12, 23, 42));
        assertEquals(actual, expected);
    }

    /**
     * Tests for DayTimeRange
     */

    @Test
    public void dayTimeRangeToJson() throws IOException {
        DayTimeRange dayTimeRange = DayTimeRange.of(DayOfWeek.MONDAY, LocalTime.of(11, 25), LocalTime.of(23, 42));
        String dump = this.objectMapper.writeValueAsString(dayTimeRange);
        assertEquals("{\"dayOfWeek\":1,\"startTime\":\"11:25\",\"endTime\":\"23:42\"}", dump);
    }

    @Test
    public void dayTimeRangeFromJson() throws IOException {
        String rawJson = "{\"dayOfWeek\":3,\"startTime\":\"11:25\",\"endTime\":\"23:53\"}";
        DayTimeRange actual = this.objectMapper.readValue(rawJson, DayTimeRange.class);
        DayTimeRange expected = DayTimeRange.of(DayOfWeek.WEDNESDAY, LocalTime.of(11, 25), LocalTime.of(23, 53));
        assertEquals(expected, actual);
    }
}