package com.timetablegenerator.tests.api.serializer;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.serializer.model.SectionSerializer;
import com.timetablegenerator.serializer.model.SerializerContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class SectionTests {

    private ObjectMapper objectMapper;

    private RepeatingPeriod rp;
    private OneTimePeriod otp;

    private Term term_fall = TermDefinition.builder("fall", "Fall", 1).build().createForYear(2016);
    private SerializerContext context;

    @Before
    public void setUp() {
        this.objectMapper = new ObjectMapper()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(SerializationFeature.INDENT_OUTPUT, true);
        this.rp = RepeatingPeriod.of(this.term_fall);
        this.otp = OneTimePeriod.of(this.term_fall);
        this.context = SerializerContext.of(School.builder("id", "name").build(),
                new Term[]{this.term_fall}, new Department[]{});
    }

    @Test
    public void serializeSection() throws IOException {

        Section section = Section.of(this.term_fall, "test_id").addNotes("note_1", "note_2")
                .setEnrollment(40).setMaximumEnrollment(40)
                .setWaiting(5)
                .setCancelled(false)
                .setOnline(false)
                .addPeriod(this.otp)
                .addPeriod(this.rp);

        SectionSerializer serializer = new SectionSerializer();
        serializer.fromInstance(section);

        String expected = "{\n" +
                "  \"cancelled\" : false,\n" +
                "  \"enrolled\" : 40,\n" +
                "  \"full\" : true,\n" +
                "  \"id\" : \"test_id\",\n" +
                "  \"maxEnrollment\" : 40,\n" +
                "  \"notes\" : [ \"note_1\", \"note_2\" ],\n" +
                "  \"oneTimePeriods\" : [ {\n" +
                "    \"term\" : \"2016/fall\"\n" +
                "  } ],\n" +
                "  \"online\" : false,\n" +
                "  \"repeatingPeriods\" : [ {\n" +
                "    \"term\" : \"2016/fall\"\n" +
                "  } ],\n" +
                "  \"term\" : \"2016/fall\",\n" +
                "  \"waitingList\" : true,\n" +
                "  \"waitingNum\" : 5\n" +
                "}";
        String actual = this.objectMapper.writeValueAsString(serializer);

        assertEquals(expected, actual);
    }

    @Test
    public void deserializeSection() throws IOException {

        Section expected = Section.of(this.term_fall, "test_id").addNotes("note_1", "note_2")
                .setEnrollment(40).setMaximumEnrollment(40)
                .setWaiting(5)
                .setCancelled(false)
                .setOnline(false)
                .addPeriod(this.otp)
                .addPeriod(this.rp);

        String raw = "{\n" +
                "  \"cancelled\" : false,\n" +
                "  \"enrolled\" : 40,\n" +
                "  \"full\" : true,\n" +
                "  \"id\" : \"test_id\",\n" +
                "  \"maxEnrollment\" : 40,\n" +
                "  \"notes\" : [ \"note_1\", \"note_2\" ],\n" +
                "  \"oneTimePeriods\" : [ {\n" +
                "    \"term\" : \"2016/fall\"\n" +
                "  } ],\n" +
                "  \"online\" : false,\n" +
                "  \"repeatingPeriods\" : [ {\n" +
                "    \"term\" : \"2016/fall\"\n" +
                "  } ],\n" +
                "  \"term\" : \"2016/fall\",\n" +
                "  \"waitingList\" : true,\n" +
                "  \"waitingNum\" : 5\n" +
                "}";

        SectionSerializer serializer = this.objectMapper.readValue(raw, SectionSerializer.class);
        Section actual = serializer.toInstance(this.context);

        assertEquals(expected, actual);
    }
}