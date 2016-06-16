package com.timetablegenerator.tests.api.diff;

import com.timetablegenerator.Settings;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.AdditionDelta;
import com.timetablegenerator.delta.ValueChangeDelta;
import com.timetablegenerator.delta.RemovalDelta;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValueDelta {

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
        ValueChangeDelta vcd = ValueChangeDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");
        assertEquals(PropertyType.CAMPUS, vcd.getPropertyType());
        assertEquals("Hello", vcd.getOldValue());
        assertEquals("Hello2", vcd.getNewValue());

        vcd = ValueChangeDelta.of(PropertyType.CREDITS, 1.25, 3.43);
        assertEquals(PropertyType.CREDITS, vcd.getPropertyType());
        assertEquals(1.25, vcd.getOldValue());
        assertEquals(3.43, vcd.getNewValue());

        vcd = ValueChangeDelta.of(PropertyType.IS_ALTERNATING, false, true);
        assertEquals(PropertyType.IS_ALTERNATING, vcd.getPropertyType());
        assertEquals(false, vcd.getOldValue());
        assertEquals(true, vcd.getNewValue());
    }

    @Test
    public void changeString() {
        ValueChangeDelta vcd = ValueChangeDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");
        assertEquals("MODIFIED [CAMPUS]\n"
                + I + "Old value : \"Hello\"\n"
                + I + "New value : \"Hello2\"", vcd.toString());
    }

    @Test
    public void changeEquality() {
        ValueChangeDelta vcd1 = ValueChangeDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");
        ValueChangeDelta vcd2 = ValueChangeDelta.of(PropertyType.NOTE, "Hello", "Hello2");
        ValueChangeDelta vcd3 = ValueChangeDelta.of(PropertyType.CAMPUS, "Hellow", "Hello2");
        ValueChangeDelta vcd4 = ValueChangeDelta.of(PropertyType.CAMPUS, "Hello", "Hello2");

        assertEquals(vcd1, vcd4);
        assertNotEquals(vcd1, vcd2);
        assertNotEquals(vcd1, vcd2);
        assertNotEquals(vcd1, vcd3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void badChangeType() {
        ValueChangeDelta.of(PropertyType.ONE_TIME_PERIOD, 1, 2);
    }
}