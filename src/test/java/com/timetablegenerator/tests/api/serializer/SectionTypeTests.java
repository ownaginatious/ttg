package com.timetablegenerator.tests.api.serializer;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timetablegenerator.model.*;
import com.timetablegenerator.serializer.model.SectionTypeSerializer;
import com.timetablegenerator.serializer.model.SerializerContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class SectionTypeTests {

    private ObjectMapper objectMapper;

    private Term term_fall = TermDefinition.builder("fall", "Fall", 1).build().createForYear(2016);
    private SerializerContext context;

    @Before
    public void setUp() {

        this.objectMapper = new ObjectMapper()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = SerializerContext.of(School.builder("id", "name")
                        .withSection("section_code", "Section name").build(),
                new Term[]{this.term_fall}, new Department[]{});
    }

    @Test
    public void serializeSectionType() throws IOException {

        Section section = Section.of(this.term_fall, "test_id");
        SectionType sectionType = SectionType.of(
                this.context.getSchool(), this.term_fall, "section_code").addSection(section);

        SectionTypeSerializer serializer = new SectionTypeSerializer();
        serializer.fromInstance(sectionType);

        String expected = "{\n" +
                "  \"code\" : \"section_code\",\n" +
                "  \"name\" : \"Section name\",\n" +
                "  \"sections\" : {\n" +
                "    \"test_id\" : {\n" +
                "      \"id\" : \"test_id\",\n" +
                "      \"term\" : \"2016/fall\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"term\" : \"2016/fall\"\n" +
                "}";
        String actual = this.objectMapper.writeValueAsString(serializer);

        assertEquals(expected, actual);
    }

    @Test
    public void deserializeSectionType() throws IOException {

        Section section = Section.of(this.term_fall, "test_id");
        SectionType expected = SectionType.of(
                this.context.getSchool(), this.term_fall, "section_code").addSection(section);

        String raw = "{\n" +
                "  \"code\" : \"section_code\",\n" +
                "  \"name\" : \"Section name\",\n" +
                "  \"sections\" : {\n" +
                "    \"test_id\" : {\n" +
                "      \"term\" : \"2016/fall\",\n" +
                "      \"id\" : \"test_id\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"term\" : \"2016/fall\"\n" +
                "}";

        SectionTypeSerializer serializer = this.objectMapper.readValue(raw, SectionTypeSerializer.class);
        SectionType actual = serializer.toInstance(this.context);

        assertEquals(expected, actual);
    }

    @Test(expected = IllegalStateException.class)
    public void deserializeInvalidMapping() throws IOException {

        String raw = "{\n" +
                "  \"code\" : \"section_code\",\n" +
                "  \"name\" : \"Section name\",\n" +
                "  \"sections\" : {\n" +
                "    \"test_id\" : {\n" +
                "      \"id\" : \"different_id\",\n" +
                "      \"term\" : \"2016/fall\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"term\" : \"2016/fall\"\n" +
                "}";

        this.objectMapper.readValue(raw, SectionTypeSerializer.class).toInstance(this.context);
    }
}