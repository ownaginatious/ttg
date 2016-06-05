package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Section;
import com.timetablegenerator.model.SectionType;
import com.timetablegenerator.model.TermClassifier;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
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

    @Before
    public void setUp() {
        school = School.builder(TestUtils.getRandomString(20),
                                TestUtils.getRandomString(10))
                .withSection("Section Type A", "A")
                .withSection("Section Type B", "B")
                .withSection("Section Type C", "C")
                .withSection("Section Type D", "D").build();
        st = SectionType.of(school, "A");
    }

    @Test()
    public void creation() {
        assertEquals(this.school, this.st.getSchool());
        assertEquals("A", this.st.getCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownSectionCodeCreation() {
        SectionType.of(this.school, "E");
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateSections(){
        Section s1 = Section.of("abc");
        Section s2 = Section.of("abc");
        this.st.addSection(s1);
        this.st.addSection(s2);
    }

    @Test
    public void keys(){
        Set<String> ids = new HashSet<>(
                Arrays.asList("abc", "def", "hij", "lmno"));
        ids.forEach(x -> this.st.addSection(Section.of(x)));
        assertEquals(ids, this.st.getSectionKeys());
    }

    @Test
    public void getSection(){
        Section s1 = Section.of("abc");
        Section s2 = Section.of("def");

        this.st.addSection(s1);
        assertEquals(s1, this.st.getSection("abc").orElse(null));
        this.st.addSection(s2);
        assertEquals(s1, this.st.getSection("abc").orElse(null));
        assertEquals(s2, this.st.getSection("def").orElse(null));
    }

    @Test
    public void compare(){

        SectionType st1 = SectionType.of(school, "A");
        SectionType st2 = SectionType.of(school, "C");
        SectionType st3 = SectionType.of(school, "D");
        SectionType st4 = SectionType.of(school, "B");

        List<SectionType> sectionTypes = Arrays.asList(st1, st2, st3, st4);
        Collections.sort(sectionTypes);
        assertEquals(sectionTypes, Arrays.asList(st1, st4, st2, st3));

        // Different name
        School school1 = School.builder("test", "test")
                .withSection("Section A", "A").build();
        School school2 = School.builder("test", "test")
                .withSection("Section C", "A").build();
        st1 = SectionType.of(school1, "A");
        st2 = SectionType.of(school2, "A");
        assertThat(st1.compareTo(st2), lessThan(0));

        // Equal
        assertEquals(0, st1.compareTo(st1));
    }

    @Test
    public void string(){
        Section s1 = Section.of("abc")
                .addNotes("Note 1", "Note 2")
                .addPeriod(OneTimePeriod.of(TermClassifier.FALL)
                                        .setCampus("Campus x")
                                        .setRoom("Room 234"));
        Section s2 = Section.of("def");
        this.st.addSection(s1).addSection(s2);
        assertEquals(
                String.format("%s sections:\n\n%s\n\n%s",
                        this.st.getName(),
                        StringUtilities.indent(1, s1.toString()),
                        StringUtilities.indent(1, s2.toString())),
                this.st.toString());
    }

    @Test
    public void emptySectionString(){
        assertEquals(String.format("%s sections:\n\n%s%s",
                this.st.getName(), I, "NONE LISTED"), this.st.toString());
    }

    @Test
    public void deltaId(){
        assertEquals(this.st.getCode(), this.st.getDeltaId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deltaUnrelated(){
        SectionType st1 = SectionType.of(school, "A");
        SectionType st2 = SectionType.of(school, "C");
        st1.findDifferences(st2);
    }

    @Test
    public void delta(){

        Section sa1 = Section.of("a");
        Section sa2 = Section.of("c").setCancelled(true);

        Section sb1 = Section.of("d");
        Section sb2 = Section.of("c")
                .addNotes("Note").setAlternating(true).setCancelled(false);

        SectionType st1 = SectionType.of(school, "A")
                .addSection(sa1).addSection(sa2);
        SectionType st2 = SectionType.of(school, "A")
                .addSection(sb1).addSection(sb2);

        assertEquals(
                StructureChangeDelta.of(PropertyType.SECTION_TYPE, st1)
                    .addAdded(PropertyType.SECTION, sb1)
                    .addRemoved(PropertyType.SECTION, sa1)
                    .addChange(StructureChangeDelta.of(PropertyType.SECTION, sa2)
                                .addAdded(PropertyType.NOTE, "Note")
                                .addAdded(PropertyType.IS_ALTERNATING, true)
                                .addValueIfChanged(PropertyType.IS_CANCELLED, true, false)),
                st1.findDifferences(st2));
    }
}
