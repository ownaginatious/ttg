package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.exceptions.TermScopeException;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

public class SectionTypeTests {

    private static String I;

    static {
        Settings.setIndentSize(4);
        I = Settings.getIndent();
    }

    private SectionType st;
    private School school;

    private Term term_fall = TermDefinition.builder("fall", "Fall", 1)
            .withSubterm(TermDefinition.builder("fall_fq", "Fall First Quarter", 2).build())
            .build().createForYear(2016);
    private Term term_fall_fq = term_fall.getSubterm("fall_fq");

    @Before
    public void setUp() {
        school = School.builder(TestUtils.getRandomString(10),
                TestUtils.getRandomString(20))
                .withSection("A", "Section Type A")
                .withSection("B", "Section Type B")
                .withSection("C", "Section Type C")
                .withSection("D", "Section Type D").build();
        st = SectionType.of(this.school, this.term_fall, "A");
    }

    @Test()
    public void creation() {
        assertEquals(this.school, this.st.getSchool());
        assertEquals("A", this.st.getCode());
        assertEquals(this.term_fall, this.st.getTerm());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownSectionCodeCreation() {
        SectionType.of(this.school, this.term_fall, "E");
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateSections() {
        Section s1 = Section.of(this.term_fall, "abc");
        Section s2 = Section.of(this.term_fall, "abc");
        this.st.addSection(s1);
        this.st.addSection(s2);
    }

    @Test(expected = TermScopeException.class)
    public void outOfScopeSection() {
        SectionType st = SectionType.of(this.school, this.term_fall_fq, "A");
        st.addSection(Section.of(this.term_fall, "abc"));
    }

    @Test
    public void values() {
        Set<String> ids = new HashSet<>(
                Arrays.asList("abc", "def", "hij", "lmno"));
        ids.forEach(x -> this.st.addSection(Section.of(this.term_fall, x)));
        assertEquals(ids, this.st.getSections().stream().map(Section::getId).collect(Collectors.toSet()));
    }

    @Test
    public void getSection() {
        Section s1 = Section.of(this.term_fall, "abc");
        Section s2 = Section.of(this.term_fall, "def");

        this.st.addSection(s1);
        assertEquals(s1, this.st.getSection("abc").orElse(null));
        this.st.addSection(s2);
        assertEquals(s1, this.st.getSection("abc").orElse(null));
        assertEquals(s2, this.st.getSection("def").orElse(null));
    }

    @Test
    public void compare() {

        SectionType st1 = SectionType.of(school, this.term_fall, "A");
        SectionType st2 = SectionType.of(school, this.term_fall, "C");
        SectionType st3 = SectionType.of(school, this.term_fall, "D");
        SectionType st4 = SectionType.of(school, this.term_fall, "B");

        List<SectionType> sectionTypes = Arrays.asList(st1, st2, st3, st4);
        Collections.sort(sectionTypes);
        assertEquals(sectionTypes, Arrays.asList(st1, st4, st2, st3));

        // Different name
        School school1 = School.builder("test", "test")
                .withSection("A", "Section A").build();
        School school2 = School.builder("test", "test")
                .withSection("A", "Section C").build();
        st1 = SectionType.of(school1, this.term_fall, "A");
        st2 = SectionType.of(school2, this.term_fall, "A");
        assertThat(st1.compareTo(st2), lessThan(0));

        // Equal
        assertEquals(0, st1.compareTo(st1));
    }

    @Test
    public void string() {
        Section s1 = Section.of(this.term_fall, "abc")
                .addNotes("Note 1", "Note 2")
                .addPeriod(OneTimePeriod.of(this.term_fall)
                        .setCampus("Campus x")
                        .setRoom("Room 234"));
        Section s2 = Section.of(this.term_fall, "def");
        this.st.addSection(s1).addSection(s2);
        assertEquals(
                String.format("%s sections:\n\n%s\n\n%s",
                        this.st.getName(),
                        StringUtilities.indent(1, s1.toString()),
                        StringUtilities.indent(1, s2.toString())),
                this.st.toString());
    }

    @Test
    public void emptySectionString() {
        assertEquals(String.format("%s sections:\n\n%s%s",
                this.st.getName(), I, "NONE LISTED"), this.st.toString());
    }

    @Test
    public void deltaId() {
        assertEquals(this.st.getCode(), this.st.getDeltaId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deltaUnrelatedCode() {
        SectionType st1 = SectionType.of(school, this.term_fall, "A");
        SectionType st2 = SectionType.of(school, this.term_fall, "C");
        st1.findDifferences(st2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deltaUnrelatedTerm() {
        SectionType st1 = SectionType.of(school, this.term_fall, "A");
        SectionType st2 = SectionType.of(school, this.term_fall_fq, "A");
        st1.findDifferences(st2);
    }

    @Test
    public void delta() {

        Section sa1 = Section.of(this.term_fall, "a");
        Section sa2 = Section.of(this.term_fall, "c").setCancelled(true);

        Section sb1 = Section.of(this.term_fall, "d");
        Section sb2 = Section.of(this.term_fall, "c")
                .addNotes("Note").setSerialNumber("testing").setCancelled(false);

        SectionType st1 = SectionType.of(school, this.term_fall, "A")
                .addSection(sa1).addSection(sa2);
        SectionType st2 = SectionType.of(school, this.term_fall, "A")
                .addSection(sb1).addSection(sb2);

        assertEquals(
                StructureDelta.of(PropertyType.SECTION_TYPE, st1)
                        .addAdded(PropertyType.SECTION, sb1)
                        .addRemoved(PropertyType.SECTION, sa1)
                        .addSubstructureChange(StructureDelta.of(PropertyType.SECTION, sa2)
                                .addAdded(PropertyType.NOTE, "Note")
                                .addAdded(PropertyType.SERIAL_NUMBER, "testing")
                                .addValueIfChanged(PropertyType.IS_CANCELLED, true, false)),
                st1.findDifferences(st2));
    }
}
