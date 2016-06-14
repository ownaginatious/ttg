package com.timetablegenerator.tests.api.diff;

import com.timetablegenerator.Settings;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.ValueAdditionDelta;
import com.timetablegenerator.delta.ValueChangeDelta;
import com.timetablegenerator.delta.ValueRemovalDelta;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValueDelta {

    private static String I = Settings.getIndent();

    @Test
    public void addition() {
        ValueAdditionDelta vad = ValueAdditionDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("Hello", vad.getNewValue());
        assertEquals(PropertyType.CAMPUS, vad.getPropertyType());

        vad = ValueAdditionDelta.of(PropertyType.CREDITS, 1.234);
        assertEquals(1.234, vad.getNewValue());
        assertEquals(PropertyType.CREDITS, vad.getPropertyType());

        vad = ValueAdditionDelta.of(PropertyType.IS_ALTERNATING, true);
        assertEquals(true, vad.getNewValue());
        assertEquals(PropertyType.IS_ALTERNATING, vad.getPropertyType());
    }

    @Test
    public void additionString() {
        ValueAdditionDelta vad = ValueAdditionDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("ADDED [CAMPUS] (value = Hello)", vad.toString());
    }

    @Test
    public void additionEquality() {
        ValueAdditionDelta vad1 = ValueAdditionDelta.of(PropertyType.CAMPUS, "Hello");
        ValueAdditionDelta vad2 = ValueAdditionDelta.of(PropertyType.NOTE, "Hello");
        ValueAdditionDelta vad3 = ValueAdditionDelta.of(PropertyType.CAMPUS, "Hellow");
        ValueAdditionDelta vad4 = ValueAdditionDelta.of(PropertyType.CAMPUS, "Hello");

        assertEquals(vad1, vad4);
        assertNotEquals(vad1, vad2);
        assertNotEquals(vad1, vad2);
        assertNotEquals(vad1, vad3);
    }

    @Test
    public void removal() {
        ValueRemovalDelta vrd = ValueRemovalDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("Hello", vrd.getOldValue());
        assertEquals(PropertyType.CAMPUS, vrd.getPropertyType());

        vrd = ValueRemovalDelta.of(PropertyType.CREDITS, 1.234);
        assertEquals(1.234, vrd.getOldValue());
        assertEquals(PropertyType.CREDITS, vrd.getPropertyType());

        vrd = ValueRemovalDelta.of(PropertyType.IS_ALTERNATING, true);
        assertEquals(true, vrd.getOldValue());
        assertEquals(PropertyType.IS_ALTERNATING, vrd.getPropertyType());
    }

    @Test
    public void removalString() {
        ValueRemovalDelta vrd = ValueRemovalDelta.of(PropertyType.CAMPUS, "Hello");
        assertEquals("REMOVED [CAMPUS] (value = Hello)", vrd.toString());
    }

    @Test
    public void removalEquality() {
        ValueRemovalDelta vrd1 = ValueRemovalDelta.of(PropertyType.CAMPUS, "Hello");
        ValueRemovalDelta vrd2 = ValueRemovalDelta.of(PropertyType.NOTE, "Hello");
        ValueRemovalDelta vrd3 = ValueRemovalDelta.of(PropertyType.CAMPUS, "Hellow");
        ValueRemovalDelta vrd4 = ValueRemovalDelta.of(PropertyType.CAMPUS, "Hello");

        assertEquals(vrd1, vrd4);
        assertNotEquals(vrd1, vrd2);
        assertNotEquals(vrd1, vrd2);
        assertNotEquals(vrd1, vrd3);
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
}