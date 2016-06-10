package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TermDefinition;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class TermTests {

    private TermDefinition td;
    private Term t;

    @Before
    public void setUp() {
        this.td = TermDefinition.builder("TERM_CODE", "TERM_NAME", 0)
                .withSubterm(TermDefinition.builder("SUBTERM_A_CODE", "SUBTERM_A_NAME", 1).build())
                .withSubterm(
                        TermDefinition.builder("SUBTERM_B_CODE", "SUBTERM_B_NAME", 2)
                                .withSubterm(
                                        TermDefinition.builder("SUBTERM_C_CODE", "SUBTERM_C_NAME", 3).build()
                                ).build()
                ).build();
        this.t = this.td.createForYear(2016);
    }

    @Test
    public void creation() {
        assertEquals("TERM_CODE", this.td.getCode());
        assertEquals("TERM_NAME", this.td.getName());
        Term t = td.createForYear(2016);
        assertEquals(2016, t.getYear());
        assertEquals(0, this.td.getYearOffset());
    }

    @Test
    public void yearOffset() {
        TermDefinition td = TermDefinition.builder("TERM_CODE", "TERM_NAME", 0)
                .withYearOffset(4).build();
        assertEquals(4, td.getYearOffset());
    }

    @Test
    public void termKey() {
        assertFalse(this.t.getKey().isPresent());
        this.t.setKey("key");
        assertEquals("key", this.t.getKey().orElse(null));
    }

    @Test
    public void dates() {
        assertFalse(this.t.getStartDate().isPresent());
        assertFalse(this.t.getEndDate().isPresent());
        this.t.setDates(LocalDate.MIN, LocalDate.MAX);
        assertEquals(LocalDate.MIN, this.t.getStartDate().orElse(null));
        assertEquals(LocalDate.MAX, this.t.getEndDate().orElse(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void badDates() {
        this.t.setDates(LocalDate.MAX, LocalDate.MIN);
    }

    @Test
    public void examDates() {
        assertFalse(this.t.getExamStartDate().isPresent());
        assertFalse(this.t.getExamEndDate().isPresent());
        this.t.setExamDates(LocalDate.MIN, LocalDate.MAX);
        assertEquals(LocalDate.MIN, this.t.getExamStartDate().orElse(null));
        assertEquals(LocalDate.MAX, this.t.getExamEndDate().orElse(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void badExamDates() {
        this.t.setExamDates(LocalDate.MAX, LocalDate.MIN);
    }

    @Test
    public void string() {
        assertEquals("TERM_NAME (TERM_CODE)", this.td.toString());
        assertEquals("TERM_NAME (TERM_CODE) 2016", this.t.toString());
        this.t.setKey("KEY");
        assertEquals("TERM_NAME (TERM_CODE) 2016 (key: KEY)", this.t.toString());
    }

    @Test
    public void subterms() {
        assertEquals(new HashSet<>(Arrays.asList("SUBTERM_A_CODE", "SUBTERM_B_CODE")),
                this.td.getSubterms());
    }

    @Test
    public void allSubterms() {
        assertEquals(new HashSet<>(Arrays.asList("SUBTERM_A_CODE", "SUBTERM_B_CODE", "SUBTERM_C_CODE")),
                this.td.getAllSubterms());
    }

    @Test(expected = IllegalArgumentException.class)
    public void recursivelyDuplicateSubterm() {
        TermDefinition.builder("TERM_CODE", "TERM_NAME", 0)
            .withSubterm(TermDefinition.builder("SUBTERM_A_CODE", "SUBTERM_A_NAME", 1).build())
            .withSubterm(
                    TermDefinition.builder("SUBTERM_B_CODE", "SUBTERM_B_NAME", 2)
                            .withSubterm(
                                    TermDefinition.builder("SUBTERM_A_CODE", "SUBTERM_C_NAME", 3).build()
                            ).build()
            ).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void termAsOwnSubterm() {
        TermDefinition.builder("TERM_CODE", "TERM_NAME", 0)
                .withSubterm(TermDefinition.builder("TERM_CODE", "TERM_NAME", 1)
                        .build()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateSubterm() {
        TermDefinition.builder("TERM_CODE", "TERM_NAME", 0)
                .withSubterm(TermDefinition.builder("TERM_A_CODE", "TERM_NAME", 1).build())
                .withSubterm(TermDefinition.builder("TERM_A_CODE", "TERM_NAME", 1).build())
                .build();
    }
}