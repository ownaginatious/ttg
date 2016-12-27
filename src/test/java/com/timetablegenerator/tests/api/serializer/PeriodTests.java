package com.timetablegenerator.tests.api.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TermDefinition;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.model.range.DateRange;
import com.timetablegenerator.model.range.DateTimeRange;
import com.timetablegenerator.model.range.DayTimeRange;
import com.timetablegenerator.serializer.model.period.OneTimePeriodSerializer;
import com.timetablegenerator.serializer.model.period.RepeatingPeriodSerializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class PeriodTests {

    private ObjectMapper objectMapper;

    private RepeatingPeriod rp;
    private OneTimePeriod otp;

    private Term term_fall = TermDefinition.builder("fall", "Fall", 1).build().createForYear(2016);
    private Map<String, Term> terms = new HashMap<>();
    private School school = School.builder("school_id", "school_name").build();

    @Before
    public void setUp(){
        this.objectMapper = new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.rp = RepeatingPeriod.of(this.term_fall);
        this.otp = OneTimePeriod.of(this.term_fall);
        this.terms.put(this.term_fall.getUniqueId(), this.term_fall);
    }

    @Test
    public void serializeRepeatingPeriod() throws IOException {
        this.rp.setCampus("Test Campus")
                .setOnline(false).addSupervisors("Supervisor 1", "Supervisor 2")
                .addNotes("Note 1", "Note 2")
                .setDayTimeRange(DayTimeRange.of(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX))
                .setActiveDateRange(DateRange.of(LocalDate.MIN, LocalDate.MAX));
        RepeatingPeriodSerializer serializer = new RepeatingPeriodSerializer();
        serializer.fromInstance(this.rp);

        String expected = "{\"supervisors\":[\"Supervisor 1\",\"Supervisor 2\"],\"notes\":[\"Note 1\",\"Note 2\"]," +
                "\"campus\":\"Test Campus\",\"online\":false,\"term\":\"2016/fall\"," +
                "\"times\":{\"dayOfWeek\":5,\"startTime\":\"00:00\",\"endTime\":\"23:59\"}," +
                "\"activeDates\":{\"startDate\":\"-999999999-01-01\",\"endDate\":\"+999999999-12-31\"}}";
        String actual = this.objectMapper.writeValueAsString(serializer);

        assertEquals(expected, actual);
    }

    @Test
    public void serializeOneTimePeriod() throws IOException {
        this.otp.setCampus("Test Campus")
                .addNotes("Note 1", "Note 2")
                .setDateTimeRange(DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX));
        OneTimePeriodSerializer serializer = new OneTimePeriodSerializer();
        serializer.fromInstance(this.otp);

        String expected = "{\"supervisors\":[],\"notes\":[\"Note 1\",\"Note 2\"],\"campus\":\"Test Campus\"," +
                "\"term\":\"2016/fall\"," +
                "\"times\":{\"startDateTime\":\"-999999999-01-01T00:00\"," +
                "\"endDateTime\":\"+999999999-12-31T23:59\"}}";
        String actual = this.objectMapper.writeValueAsString(serializer);

        assertEquals(expected, actual);
    }

    @Test
    public void deserializeRepeatingPeriod() throws IOException {

        this.rp.setCampus("Test Campus")
                .setOnline(false).addSupervisors("Supervisor 1", "Supervisor 2")
                .addNotes("Note 1", "Note 2")
                .setDayTimeRange(DayTimeRange.of(DayOfWeek.FRIDAY, LocalTime.MIN, LocalTime.MAX))
                .setActiveDateRange(DateRange.of(LocalDate.MIN, LocalDate.MAX));

        String raw = "{\"supervisors\":[\"Supervisor 1\",\"Supervisor 2\"],\"notes\":[\"Note 1\",\"Note 2\"]," +
                "\"campus\":\"Test Campus\",\"online\":false,\"term\":\"2016/fall\"," +
                "\"times\":{\"dayOfWeek\":5,\"startTime\":\"00:00\",\"endTime\":\"23:59\"}," +
                "\"activeDates\":{\"startDate\":\"-999999999-01-01\",\"endDate\":\"+999999999-12-31\"}}";

        RepeatingPeriodSerializer serializer = this.objectMapper.readValue(raw, RepeatingPeriodSerializer.class);
        RepeatingPeriod actual = serializer.toInstance(this.school, this.terms);

        assertEquals(this.rp, actual);
    }

    @Test
    public void deserializeOneTimePeriod() throws IOException {

        this.otp.setCampus("Test Campus")
                .addNotes("Note 1", "Note 2")
                .setDateTimeRange(DateTimeRange.of(LocalDateTime.MIN, LocalDateTime.MAX));

        String raw = "{\"supervisors\":[],\"notes\":[\"Note 1\",\"Note 2\"],\"campus\":\"Test Campus\"," +
                "\"term\":\"2016/fall\"," +
                "\"times\":{\"startDateTime\":\"-999999999-01-01T00:00\"," +
                "\"endDateTime\":\"+999999999-12-31T23:59\"}}";

        OneTimePeriodSerializer serializer = this.objectMapper.readValue(raw, OneTimePeriodSerializer.class);
        OneTimePeriod actual = serializer.toInstance(this.school, this.terms);

        assertEquals(this.otp, actual);
    }
}