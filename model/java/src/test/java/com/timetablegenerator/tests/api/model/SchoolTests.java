package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TermDefinition;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static junit.framework.TestCase.*;

public class SchoolTests {

    private TermDefinition td1, td2;
    private School s;

    @Before
    public void setUp() {
        this.td1 = TermDefinition.builder("SEM1", "SEM1", 1).withYearOffset(-1)
                .withSubterm(TermDefinition.builder("SEM1Q1", "SEM1Q1", 2).build())
                .withSubterm(TermDefinition.builder("SEM1Q2", "SEM1Q2", 3)
                        .withSubterm(TermDefinition.builder("SEM1Q2Q1", "SEM1Q2Q1", 4)
                                .withYearOffset(4).build())
                        .build())
                .build();

        this.td2 = TermDefinition.builder("SEM2", "SEM2", 5).withYearOffset(1)
                .withSubterm(TermDefinition.builder("SEM2Q1", "SEM2Q1", 6).build())
                .withSubterm(TermDefinition.builder("SEM2Q2", "SEM2Q2", 7).build())
                .build();

        this.s = School.builder("test", "Test School")
                .withTerm(this.td1).withTerm(this.td2)
                .withSection("A", "Section A")
                .withSection("B", "Section B")
                .withSection("C", "Section C")
                .withSection("D", "Section D").build();
    }

    @Test
    public void creation() {
        assertEquals("test", this.s.getId());
        assertEquals("Test School", this.s.getName());
    }

    @Test
    public void sectionCodes() {
        assertEquals(new HashSet<>(Arrays.asList("A", "B", "C", "D")), this.s.getSectionTypeCodes());
    }

    @Test
    public void terms() {
        assertEquals(new HashSet<>(
                Arrays.asList("SEM1", "SEM1Q1", "SEM1Q2", "SEM1Q2Q1",
                        "SEM2", "SEM2Q1", "SEM2Q2")),
                this.s.getTermCodes()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void conflictingTerms() {
        TermDefinition td1 = TermDefinition.builder("SEM1", "SEM1", 1).withYearOffset(-1)
                .withSubterm(TermDefinition.builder("SEM1Q1", "SEM1Q1", 2).build())
                .withSubterm(TermDefinition.builder("SEM1Q1", "SEM1Q1", 3)
                        .withSubterm(TermDefinition.builder("SEM1Q2Q1", "SEM1Q2Q1", 4)
                                .withYearOffset(4).build())
                        .build())
                .build();

        TermDefinition td2 = TermDefinition.builder("SEM2", "SEM2", 5).withYearOffset(1)
                .withSubterm(TermDefinition.builder("SEM2Q1", "SEM2Q1", 6).build())
                .withSubterm(TermDefinition.builder("SEM2Q2", "SEM2Q2", 7).build())
                .build();

        School.builder("test", "Test School")
                .withTerm(td1).withTerm(td2).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void conflictingSections() {
        School.builder("test", "Test School")
                .withSection("CODE", "C1")
                .withSection("CODE", "C1").build();
    }

    @Test
    public void termInstance() {
        Term t1 = this.s.getTermInstance("SEM1Q2Q1", 2016);
        assertEquals("SEM1Q2Q1", t1.getTermDefinition().getCode());
        assertEquals(2016, t1.getYear());
        Term t2 = this.s.getTermInstance("SEM2Q2", 2014);
        assertEquals("SEM2Q2", t2.getTermDefinition().getCode());
        assertEquals(2014, t2.getYear());
    }

    @Test
    public void sectionNames(){
        assertEquals("Section A", this.s.getSectionTypeName("A"));
        assertEquals("Section B", this.s.getSectionTypeName("B"));
        assertEquals("Section C", this.s.getSectionTypeName("C"));
        assertEquals("Section D", this.s.getSectionTypeName("D"));
    }

    @Test
    public void compare(){
        School s1 = School.builder("A", "School A").build();
        School s2 = School.builder("B", "School B").build();
        School s3 = School.builder("B", "School A").build();

        List<School> schools = Arrays.asList(s2, s1, s3, s1);
        Collections.sort(schools);

        assertEquals(Arrays.asList(s1, s1, s3, s2), schools);
    }

    @Test
    public void string(){
        assertEquals("* School: Test School [test]\n" +
                "* Section types:\n" +
                "    - A -> Section A\n" +
                "    - B -> Section B\n" +
                "    - C -> Section C\n" +
                "    - D -> Section D\n" +
                "* Terms:\n" +
                "    - SEM1 (SEM1)\n" +
                "    - SEM2 (SEM2)", this.s.toString());
    }
}