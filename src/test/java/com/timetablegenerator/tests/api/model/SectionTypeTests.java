package com.timetablegenerator.tests.api.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Section;
import com.timetablegenerator.model.SectionType;
import com.timetablegenerator.tests.api.TestUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SectionTypeTests {

    private static String I;

    static {
        Settings.setIndentSize(4);
        I = Settings.getIndent();
    }

    private SectionType st1, st2;
    private School school;

    @Before
    public void setUp() {
        school = School.builder(TestUtils.getRandomString(20),
                                TestUtils.getRandomString(10))
                .withSection("Section A", "A")
                .withSection("Section B", "B")
                .withSection("Section C", "C").build();
        st1 = new SectionType(school, "A");
    }

    @Test()
    public void creation() {
        assertEquals(this.school, this.st1.getSchool());
        assertEquals("A", this.st1.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownSectionCodeCreation() {
        new SectionType(this.school, "D");
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateSections(){
        Section s1 = Section.fromSectionId("abc");
        Section s2 = Section.fromSectionId("abc");
        this.st1.addSection(s1);
        this.st1.addSection(s2);
    }

    @Test
    public void keys(){

    }
}
