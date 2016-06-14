package com.timetablegenerator.tests.api.diff;

import com.timetablegenerator.Settings;
import com.timetablegenerator.delta.*;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TermDefinition;
import com.timetablegenerator.model.period.RepeatingPeriod;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StructuralDelta {

    private static String I = Settings.getIndent();

    private Term term = TermDefinition.builder("SEM1", "Semester 1", 0)
            .build().createForYear(2016);
    private RepeatingPeriod rp1 = RepeatingPeriod.of(this.term).setCampus("Hello");
    private RepeatingPeriod rp2 = RepeatingPeriod.of(this.term).setCampus("Hello 2");

    @Test
    public void addition() {
        StructureAdditionDelta sad =
                StructureAdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals(PropertyType.REPEATING_PERIOD, sad.getPropertyType());
        assertEquals(this.rp1, sad.getNewValue());
    }

    @Test
    public void additionString() {
        StructureAdditionDelta sad =
                StructureAdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals("ADDED [REPEATING_PERIOD] (id = SEM1/2016/TBA/TBA/TBA)", sad.toString());
    }

    @Test
    public void additionEquality(){
        StructureAdditionDelta sad =
                StructureAdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        StructureAdditionDelta sad1 =
                StructureAdditionDelta.of(PropertyType.ONE_TIME_PERIOD, this.rp1);
        StructureAdditionDelta sad2 =
                StructureAdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp2);
        StructureAdditionDelta sad3 =
                StructureAdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);

        assertEquals(sad, sad3);
        assertNotEquals(sad, sad1);
        assertNotEquals(sad, sad2);
    }

    @Test
    public void removal() {
        StructureRemovalDelta srd =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals(PropertyType.REPEATING_PERIOD, srd.getPropertyType());
        assertEquals(this.rp1, srd.getOldValue());
    }

    @Test
    public void removalString() {
        StructureRemovalDelta srd =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals("REMOVED [REPEATING_PERIOD] (id = SEM1/2016/TBA/TBA/TBA)", srd.toString());
    }

    @Test
    public void removalEquality(){
        StructureRemovalDelta srd =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        StructureRemovalDelta srd1 =
                StructureRemovalDelta.of(PropertyType.ONE_TIME_PERIOD, this.rp1);
        StructureRemovalDelta srd2 =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp2);
        StructureRemovalDelta srd3 =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);

        assertEquals(srd, srd3);
        assertNotEquals(srd, srd1);
        assertNotEquals(srd, srd2);
    }
}