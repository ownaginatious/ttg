package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TermDefinition;
import com.timetablegenerator.model.range.DateRange;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TermTests {

    private TermDefinition td;
    private Term t;

    @Before
    public void setUp() {
        this.td = TermDefinition.builder("TERM_CODE", "TERM_NAME", 0)
                .withSubterm(TermDefinition.builder("SEM1", "SEM1", 1).withYearOffset(-1)
                        .withSubterm(TermDefinition.builder("SEM1Q1", "SEM1Q1", 2).build())
                        .withSubterm(TermDefinition.builder("SEM1Q2", "SEM1Q2", 3)
                                .withSubterm(TermDefinition.builder("SEM1Q2Q1", "SEM1Q2Q1", 4)
                                        .withYearOffset(4).build())
                                .build())
                        .build()
                )
                .withSubterm(TermDefinition.builder("SEM2", "SEM2", 5).withYearOffset(1)
                        .withSubterm(TermDefinition.builder("SEM2Q1", "SEM2Q1", 6).build())
                        .withSubterm(TermDefinition.builder("SEM2Q2", "SEM2Q2", 7).build())
                        .build()
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
        assertFalse(this.t.getDateRange().isPresent());
        this.t.setDateRange(DateRange.of(LocalDate.MIN, LocalDate.MAX));
        DateRange expected = this.t.getDateRange().orElse(null);
        assertEquals(LocalDate.MIN, expected.getStartDate());
        assertEquals(LocalDate.MAX, expected.getEndDate());
    }

    @Test
    public void examDates() {

        assertFalse(this.t.getExamDateRange().isPresent());
        this.t.setExamDateRange(DateRange.of(LocalDate.MIN, LocalDate.MAX));
        DateRange expected = this.t.getExamDateRange().orElse(null);
        assertEquals(LocalDate.MIN, expected.getStartDate());
        assertEquals(LocalDate.MAX, expected.getEndDate());
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
        assertEquals(new HashSet<>(Arrays.asList("SEM1", "SEM2")),
                this.td.getSubterms());
    }

    @Test
    public void allSubterms() {
        assertEquals(new HashSet<>(Arrays.asList("SEM1", "SEM2", "SEM1Q1", "SEM1Q2",
                "SEM2Q1", "SEM2Q2", "SEM1Q2Q1")),
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

    @Test
    public void yearOffsets() {
        assertEquals(2016, this.t.getYear());
        for (String code : Arrays.asList("SEM1", "SEM1Q1", "SEM1Q2")){
            assertEquals(2015, this.t.getSubterm(code).getYear());
        }
        for (String code : Arrays.asList("SEM2", "SEM2Q1", "SEM2Q2")){
            assertEquals(2017, this.t.getSubterm(code).getYear());
        }
        assertEquals(2019, this.t.getSubterm("SEM1Q2Q1").getYear());
    }

    @Test
    public void comparison() {
        Term t1 = this.t.getSubterm("SEM1Q2");
        Term t2 = this.t.getSubterm("SEM1Q1");
        Term t3 = this.t.getSubterm("SEM1");
        Term t4 = this.t.getSubterm("SEM2Q1");
        Term t5 = this.t.getSubterm("SEM2");
        Term t6 = this.t.getSubterm("SEM2Q2");
        Term t7 = this.t;
        Term t8 = this.t.getSubterm("SEM1Q2Q1");

        List<Term> terms = Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8);
        Collections.sort(terms);
        assertEquals(Arrays.asList(t7, t3, t2, t1, t8, t5, t4, t6), terms);

        t1 = this.td.createForYear(2016).setKey("abd");
        t2 = this.td.createForYear(2016);
        t3 = this.td.createForYear(2016).setKey("abc");
        t4 = this.td.createForYear(2015);

        terms = Arrays.asList(t4, t1, t2, t3, t4);
        Collections.sort(terms);
        assertEquals(Arrays.asList(t4, t4, t2, t3, t1), terms);
    }

    @Test
    public void compareDefinition() {
        TermDefinition td1 = TermDefinition.builder("ABC", "EFG", 0).build();
        TermDefinition td2 = TermDefinition.builder("ABC1", "EFG1", 0).build();
        TermDefinition td3 = TermDefinition.builder("ABC", "EFG1", 0).build();

        List<TermDefinition> termDefinitions = Arrays.asList(td3, td1, td2, td3);
        Collections.sort(termDefinitions);
        assertEquals(Arrays.asList(td1, td3, td3, td2), termDefinitions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badSubterm() {
        this.td.getSubterm("Not a real code");
    }
}