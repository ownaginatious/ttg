package com.timetablegenerator.tests.api.diff;

import com.timetablegenerator.Settings;
import com.timetablegenerator.delta.*;
import com.timetablegenerator.model.Section;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ValueDeltaTests {

    private static String I = Settings.getIndent();

    @Test
    public void addition() {
        AdditionDelta vad = AdditionDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("Hello", vad.getNewValue());
        assertEquals(PropertyType.CAMPUS, vad.getPropertyType());

        vad = AdditionDelta.of(PropertyType.CREDITS, 1.234);
        assertEquals(1.234, vad.getNewValue());
        assertEquals(PropertyType.CREDITS, vad.getPropertyType());

        vad = AdditionDelta.of(PropertyType.IS_ALTERNATING, true);
        assertEquals(true, vad.getNewValue());
        assertEquals(PropertyType.IS_ALTERNATING, vad.getPropertyType());
    }

    @Test
    public void additionString() {
        AdditionDelta vad = AdditionDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("ADDED [CAMPUS] (value = Hello)", vad.toString());
    }

    @Test
    public void additionEquality() {
        AdditionDelta vad1 = AdditionDelta.of(PropertyType.CAMPUS, "Hello");
        AdditionDelta vad2 = AdditionDelta.of(PropertyType.NOTE, "Hello");
        AdditionDelta vad3 = AdditionDelta.of(PropertyType.CAMPUS, "Hellow");
        AdditionDelta vad4 = AdditionDelta.of(PropertyType.CAMPUS, "Hello");

        assertEquals(vad1, vad4);
        assertNotEquals(vad1, vad2);
        assertNotEquals(vad1, vad2);
        assertNotEquals(vad1, vad3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badAdditionType() {
        AdditionDelta.of(PropertyType.ONE_TIME_PERIOD, 1);
    }

    @Test
    public void removal() {
        RemovalDelta vrd = RemovalDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("Hello", vrd.getOldValue());
        assertEquals(PropertyType.CAMPUS, vrd.getPropertyType());

        vrd = RemovalDelta.of(PropertyType.CREDITS, 1.234);
        assertEquals(1.234, vrd.getOldValue());
        assertEquals(PropertyType.CREDITS, vrd.getPropertyType());

        vrd = RemovalDelta.of(PropertyType.IS_ALTERNATING, true);
        assertEquals(true, vrd.getOldValue());
        assertEquals(PropertyType.IS_ALTERNATING, vrd.getPropertyType());
    }

    @Test
    public void removalString() {
        RemovalDelta vrd = RemovalDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("REMOVED [CAMPUS] (value = Hello)", vrd.toString());
    }

    @Test
    public void removalEquality() {
        RemovalDelta vrd1 = RemovalDelta.of(PropertyType.CAMPUS, "Hello");
        RemovalDelta vrd2 = RemovalDelta.of(PropertyType.NOTE, "Hello");
        RemovalDelta vrd3 = RemovalDelta.of(PropertyType.CAMPUS, "Hellow");
        RemovalDelta vrd4 = RemovalDelta.of(PropertyType.CAMPUS, "Hello");

        assertEquals(vrd1, vrd4);
        assertNotEquals(vrd1, vrd2);
        assertNotEquals(vrd1, vrd2);
        assertNotEquals(vrd1, vrd3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badRemovalType() {
        RemovalDelta.of(PropertyType.ONE_TIME_PERIOD, 1);
    }

    @Test
    public void change() {
        ReplaceDelta vcd = ReplaceDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");
        assertEquals(PropertyType.CAMPUS, vcd.getPropertyType());
        assertEquals("Hello", vcd.getOldValue());
        assertEquals("Hello2", vcd.getNewValue());

        vcd = ReplaceDelta.of(PropertyType.CREDITS, 1.25, 3.43);
        assertEquals(PropertyType.CREDITS, vcd.getPropertyType());
        assertEquals(1.25, vcd.getOldValue());
        assertEquals(3.43, vcd.getNewValue());

        vcd = ReplaceDelta.of(PropertyType.IS_ALTERNATING, false, true);
        assertEquals(PropertyType.IS_ALTERNATING, vcd.getPropertyType());
        assertEquals(false, vcd.getOldValue());
        assertEquals(true, vcd.getNewValue());
    }

    @Test
    public void changeString() {
        ReplaceDelta vcd = ReplaceDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");
        assertEquals("REPLACED [CAMPUS]\n"
                + I + "Old value : \"Hello\"\n"
                + I + "New value : \"Hello2\"", vcd.toString());
    }

    @Test
    public void changeEquality() {
        ReplaceDelta vcd1 = ReplaceDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");
        ReplaceDelta vcd2 = ReplaceDelta.of(PropertyType.NOTE, "Hello", "Hello2");
        ReplaceDelta vcd3 = ReplaceDelta.of(PropertyType.CAMPUS, "Hellow", "Hello2");
        ReplaceDelta vcd4 = ReplaceDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");

        assertEquals(vcd1, vcd4);
        assertNotEquals(vcd1, vcd2);
        assertNotEquals(vcd1, vcd2);
        assertNotEquals(vcd1, vcd3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badChangeType() {
        ReplaceDelta.of(PropertyType.ONE_TIME_PERIOD, 1, 2);
    }

    @Test
    public void additionDeltaComparison() {

        AdditionDelta a1 = AdditionDelta.of(PropertyType.NUM_ENROLLED, 1);
        AdditionDelta a2 = AdditionDelta.of(PropertyType.NUM_ENROLLED, 2);
        assertEquals(0, a1.compareTo(a1));

        assertThat(a1.compareTo(a2), lessThan(0));

        AdditionDelta a3 = AdditionDelta.of(PropertyType.CREDITS, 2.0);
        assertThat(a3.compareTo(a1), lessThan(0));
        assertThat(a3.compareTo(a2), lessThan(0));
        assertThat(a1.compareTo(a3), greaterThan(0));
        assertThat(a1.compareTo(a3), greaterThan(0));
    }

    @Test
    public void removalDeltaComparison() {

        RemovalDelta r1 = RemovalDelta.of(PropertyType.NUM_ENROLLED, 1);
        RemovalDelta r2 = RemovalDelta.of(PropertyType.NUM_ENROLLED, 2);
        assertEquals(0, r1.compareTo(r1));

        assertThat(r1.compareTo(r2), lessThan(0));

        RemovalDelta r3 = RemovalDelta.of(PropertyType.CREDITS, 2.0);
        assertThat(r3.compareTo(r1), lessThan(0));
        assertThat(r3.compareTo(r2), lessThan(0));
        assertThat(r1.compareTo(r3), greaterThan(0));
        assertThat(r1.compareTo(r3), greaterThan(0));
    }

    @Test
    public void replaceDeltaComparison() {

        ReplaceDelta c1 = ReplaceDelta.of(PropertyType.NUM_ENROLLED, 0, 1);
        ReplaceDelta c2 = ReplaceDelta.of(PropertyType.NUM_ENROLLED, 0, 2);
        assertEquals(0, c1.compareTo(c1));

        assertThat(c1.compareTo(c2), lessThan(0));
        assertThat(c2.compareTo(c1), greaterThan(0));

        ReplaceDelta c3 = ReplaceDelta.of(PropertyType.CREDITS, 2.0, 3.0);
        assertThat(c3.compareTo(c1), lessThan(0));
        assertThat(c3.compareTo(c2), lessThan(0));
        assertThat(c1.compareTo(c3), greaterThan(0));
        assertThat(c1.compareTo(c3), greaterThan(0));
    }

    @Test
    public void interDeltaComparison() {

        AdditionDelta a1 = AdditionDelta.of(PropertyType.NUM_ENROLLED, 1);
        RemovalDelta r1 = RemovalDelta.of(PropertyType.NUM_ENROLLED, 1);
        ReplaceDelta c1 = ReplaceDelta.of(PropertyType.NUM_ENROLLED, 1, 2);

        assertThat(r1.compareTo(a1), greaterThan(0));
        assertThat(a1.compareTo(r1), lessThan(0));

        assertThat(c1.compareTo(a1), greaterThan(0));
        assertThat(a1.compareTo(c1), lessThan(0));

        assertThat(c1.compareTo(r1), greaterThan(0));
        assertThat(r1.compareTo(c1), lessThan(0));

        StructureDelta sd = StructureDelta.of(PropertyType.SECTION, Section.of("ABC"));

        assertThat(a1.compareTo(sd), lessThan(0));
        assertThat(sd.compareTo(a1), greaterThan(0));

        assertThat(r1.compareTo(sd), lessThan(0));
        assertThat(sd.compareTo(r1), greaterThan(0));

        assertThat(c1.compareTo(sd), lessThan(0));
        assertThat(sd.compareTo(c1), greaterThan(0));
    }
}