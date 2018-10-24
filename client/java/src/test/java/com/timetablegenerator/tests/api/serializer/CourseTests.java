package com.timetablegenerator.tests.api.serializer;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timetablegenerator.exceptions.TermScopeException;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.serializer.model.CourseSerializer;
import com.timetablegenerator.serializer.model.SerializerContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class CourseTests {

    private ObjectMapper objectMapper;

    private Term term_fall = TermDefinition.builder("fall", "Fall", 1)
            .withSubterm(TermDefinition.builder("fall_fq", "Fall First Quarted", 2).build())
            .build().createForYear(2016);
    private Term term_fall_fq = term_fall.getSubterm("fall_fq");
    private Department department = Department.of("TEST", "Test Department");
    private SerializerContext context;

    @Before
    public void setUp() {
        this.objectMapper = new ObjectMapper()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(SerializationFeature.INDENT_OUTPUT, true);

        this.context = SerializerContext.of(School.builder("id", "name")
                        .withSection("test_section_type", "Test Type")
                        .withSection("test_section_type_2", "Test Type 2").build(),
                new Term[]{this.term_fall, this.term_fall_fq}, new Department[]{this.department});
    }

    @Test
    public void serializeCourse() throws IOException {

        Course course = Course.of(
                this.context.getSchool(), this.term_fall, this.department, "course_code", "Course name")
                .addSection("test_section_type",
                        Section.of(this.term_fall, "test_section").addPeriod(RepeatingPeriod.of(this.term_fall))
                ).setDescription("test course is test").setCredits(3.14);

        CourseSerializer serializer = new CourseSerializer();
        serializer.fromInstance(course);

        String expected = "{\n" +
                "  \"code\" : \"course_code\",\n" +
                "  \"credits\" : 3.14,\n" +
                "  \"department\" : \"TEST\",\n" +
                "  \"description\" : \"test course is test\",\n" +
                "  \"name\" : \"Course name\",\n" +
                "  \"sectionTypes\" : {\n" +
                "    \"test_section_type\" : {\n" +
                "      \"code\" : \"test_section_type\",\n" +
                "      \"name\" : \"Test Type\",\n" +
                "      \"sections\" : {\n" +
                "        \"test_section\" : {\n" +
                "          \"id\" : \"test_section\",\n" +
                "          \"repeatingPeriods\" : [ {\n" +
                "            \"term\" : \"2016/fall\"\n" +
                "          } ],\n" +
                "          \"term\" : \"2016/fall\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"term\" : \"2016/fall\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"term\" : \"2016/fall\"\n" +
                "}";

        String actual = this.objectMapper.writeValueAsString(serializer);

        assertEquals(expected, actual);
    }

    @Test
    public void deserializeCourse() throws IOException {

        Course expected = Course.of(
                this.context.getSchool(), this.term_fall, this.department, "course_code", "Course name")
                .addSection("test_section_type",
                        Section.of(this.term_fall, "test_section").addPeriod(RepeatingPeriod.of(this.term_fall))
                ).setCredits(5.5).addNotes("note_1", "note_2");

        String raw = "{\n" +
                "  \"term\" : \"2016/fall\",\n" +
                "  \"department\" : \"TEST\",\n" +
                "  \"code\" : \"course_code\",\n" +
                "  \"credits\" : 5.5,\n" +
                "  \"name\" : \"Course name\",\n" +
                "  \"notes\" : [\"note_1\", \"note_2\"],\n" +
                "  \"sectionTypes\" : {\n" +
                "    \"test_section_type\" : {\n" +
                "      \"code\" : \"test_section_type\",\n" +
                "      \"name\" : \"Test Type\",\n" +
                "      \"sections\" : {\n" +
                "        \"test_section\" : {\n" +
                "          \"id\" : \"test_section\",\n" +
                "          \"repeatingPeriods\" : [ {\n" +
                "            \"term\" : \"2016/fall\"\n" +
                "          } ],\n" +
                "          \"term\" : \"2016/fall\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"term\" : \"2016/fall\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        CourseSerializer serializer = this.objectMapper.readValue(raw, CourseSerializer.class);
        Course actual = serializer.toInstance(this.context);

        assertEquals(expected, actual);
    }

    private void assertContainsOnlyCourses(Map<String, Course> courses, Course... expected) {

        assertEquals(courses.size(), expected.length);
        assertEquals(courses.keySet(),
                Arrays.stream(expected).map(Course::getUniqueId).collect(Collectors.toSet()));
    }

    @Test
    public void testPopulateDeserializedCourse() throws IOException {

        List<Course> courses = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            courses.add(Course.of(
                    this.context.getSchool(), this.term_fall, this.department, "course_code_" + i, "Course name"));
        }

        Course c1 = courses.get(0);
        c1.addAntirequisite(courses.get(1));
        c1.addPrerequisite(courses.get(2));
        c1.addCorequesite(courses.get(3));
        c1.addCrossListing(courses.get(4));
        c1.addCrossListing(courses.get(5));

        // Dump the course to a string.
        CourseSerializer serializer = new CourseSerializer();
        serializer.fromInstance(c1);
        String serializedCourse = this.objectMapper.writeValueAsString(serializer);

        // Rebuild the course.
        serializer = this.objectMapper.readValue(serializedCourse, CourseSerializer.class);
        Course loadedCourse = serializer.toInstance(this.context);

        // Assert that the requisites are unpopulated.
        assertTrue(loadedCourse.getAntirequisites().isEmpty());
        assertTrue(loadedCourse.getPrerequisites().isEmpty());
        assertTrue(loadedCourse.getCorequisites().isEmpty());
        assertTrue(loadedCourse.getCrossListings().isEmpty());

        // Populate the course requisites.
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getUniqueId, x -> x));

        serializer.populateRequisites(courseMap, loadedCourse);

        // Assert that the requisites are as expected.
        this.assertContainsOnlyCourses(loadedCourse.getAntirequisites(), courses.get(1));
        this.assertContainsOnlyCourses(loadedCourse.getPrerequisites(), courses.get(2));
        this.assertContainsOnlyCourses(loadedCourse.getCorequisites(), courses.get(3));
        this.assertContainsOnlyCourses(loadedCourse.getCrossListings(), courses.get(4), courses.get(5));
    }

    @Test(expected = IllegalStateException.class)
    public void deserializeInvalidMapping() throws IOException {

        String raw = "{\n" +
                "  \"term\" : \"2016/fall\",\n" +
                "  \"department\" : \"TEST\",\n" +
                "  \"code\" : \"course_code\",\n" +
                "  \"credits\" : 5.5,\n" +
                "  \"name\" : \"Course name\",\n" +
                "  \"notes\" : [\"note_1\", \"note_2\"],\n" +
                "  \"sectionTypes\" : {\n" +
                "    \"test_section_type\" : {\n" +
                "      \"code\" : \"test_section_type_2\",\n" +
                "      \"name\" : \"Test Type\",\n" +
                "      \"sections\" : {\n" +
                "        \"test_section\" : {\n" +
                "          \"id\" : \"test_section\",\n" +
                "          \"repeatingPeriods\" : [ {\n" +
                "            \"term\" : \"2016/fall\"\n" +
                "          } ],\n" +
                "          \"term\" : \"2016/fall\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"term\" : \"2016/fall\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        this.objectMapper.readValue(raw, CourseSerializer.class).toInstance(this.context);
    }

    @Test(expected = TermScopeException.class)
    public void outOfScopeSectionType() throws IOException {

        String raw = "{\n" +
                "  \"term\" : \"2016/fall_fq\",\n" +
                "  \"department\" : \"TEST\",\n" +
                "  \"code\" : \"course_code\",\n" +
                "  \"credits\" : 5.5,\n" +
                "  \"name\" : \"Course name\",\n" +
                "  \"notes\" : [\"note_1\", \"note_2\"],\n" +
                "  \"sectionTypes\" : {\n" +
                "    \"test_section_type\" : {\n" +
                "      \"code\" : \"test_section_type\",\n" +
                "      \"name\" : \"Test Type\",\n" +
                "      \"sections\" : {\n" +
                "        \"test_section\" : {\n" +
                "          \"id\" : \"test_section\",\n" +
                "          \"repeatingPeriods\" : [ {\n" +
                "            \"term\" : \"2016/fall\"\n" +
                "          } ],\n" +
                "          \"term\" : \"2016/fall\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"term\" : \"2016/fall\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        this.objectMapper.readValue(raw, CourseSerializer.class).toInstance(this.context);
    }
}