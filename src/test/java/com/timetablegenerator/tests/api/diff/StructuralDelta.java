package com.timetablegenerator.tests.api.diff;

import com.timetablegenerator.Settings;
import com.timetablegenerator.delta.*;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StructuralDelta {

    private static String I = Settings.getIndent();

    private Term term = TermDefinition.builder("SEM1", "Semester 1", 0)
            .build().createForYear(2016);
    private RepeatingPeriod rp1 = RepeatingPeriod.of(this.term).setCampus("Hello");
    private RepeatingPeriod rp2 = RepeatingPeriod.of(this.term).setCampus("Hello 2");
    private StructureDelta scd;

    @Before
    public void setUp(){
        this.scd = StructureDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
    }

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
        assertEquals("ADDED [REPEATING_PERIOD] (id = 2016/SEM1/TBA/TBA/TBA)", sad.toString());
    }

    @Test
    public void additionEquality(){
        StructureAdditionDelta sad =
                StructureAdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        StructureAdditionDelta sad1 =
                StructureAdditionDelta.of(PropertyType.ONE_TIME_PERIOD, OneTimePeriod.of(this.term));
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
        assertEquals("REMOVED [REPEATING_PERIOD] (id = 2016/SEM1/TBA/TBA/TBA)", srd.toString());
    }

    @Test
    public void removalEquality(){
        StructureRemovalDelta srd =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        StructureRemovalDelta srd1 =
                StructureRemovalDelta.of(PropertyType.ONE_TIME_PERIOD, OneTimePeriod.of(this.term));
        StructureRemovalDelta srd2 =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp2);
        StructureRemovalDelta srd3 =
                StructureRemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);

        assertEquals(srd, srd3);
        assertNotEquals(srd, srd1);
        assertNotEquals(srd, srd2);
    }

    @Test
    public void numericPropertyChanges() {

        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.CREDITS, 1.0, 1.3);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(ValueChangeDelta.class, this.scd.getValueChanges().stream()
                        .findFirst().orElse(null).getClass());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.CREDITS, 1.0, 1.0);
        assertEquals(0, this.scd.getValueChanges().size());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.CREDITS, null, 1.0);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(AdditionDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.CREDITS, 1.0, null);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(RemovalDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());
    }

    @Test
    public void booleanPropertyChanges() {

        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.IS_ALTERNATING, false, true);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(ValueChangeDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.IS_ALTERNATING, 1.0, 1.0);
        assertEquals(0, this.scd.getValueChanges().size());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.IS_ALTERNATING, null, false);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(AdditionDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.IS_ALTERNATING, true, null);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(RemovalDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());
    }

    @Test
    public void stringPropertyChanges() {

        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.SERIAL_NUMBER, "AAA", "BBB");
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(ValueChangeDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.SERIAL_NUMBER, "AAA", "AAA");
        assertEquals(0, this.scd.getValueChanges().size());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.SERIAL_NUMBER, null, "AAA");
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(AdditionDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());

        this.setUp();
        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.SERIAL_NUMBER, "AAA", null);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(RemovalDelta.class, this.scd.getValueChanges().stream()
                .findFirst().orElse(null).getClass());
    }

    @Test
    public void structureChangeCreation() {
        assertEquals("2016/SEM1/TBA/TBA/TBA", this.scd.getIdentifier());
    }

    @Test
    public void structureAddedProperties() {
        this.scd.addAdded(PropertyType.CAMPUS, "Hello");
        this.scd.addAdded(PropertyType.IS_ALTERNATING, true);
        this.scd.addAdded(PropertyType.CREDITS, 1.234);
        this.scd.addAdded(PropertyType.REPEATING_PERIOD, this.rp1);

        AdditionDelta delta =
                (AdditionDelta) this.scd.getValueChanges().stream()
                .filter(x -> x.getPropertyType() == PropertyType.CAMPUS)
                .findFirst().orElse(null);
        assertEquals("Hello", delta.getNewValue());

        delta = (AdditionDelta) this.scd.getValueChanges().stream()
                    .filter(x -> x.getPropertyType() == PropertyType.IS_ALTERNATING)
                    .findFirst().orElse(null);
        assertEquals(true, delta.getNewValue());

        delta = (AdditionDelta) this.scd.getValueChanges().stream()
                    .filter(x -> x.getPropertyType() == PropertyType.CREDITS)
                    .findFirst().orElse(null);
        assertEquals(1.234, delta.getNewValue());

        StructureAdditionDelta structureDelta =
                (StructureAdditionDelta) this.scd.getValueChanges().stream()
                        .filter(x -> x.getPropertyType() == PropertyType.REPEATING_PERIOD)
                        .findFirst().orElse(null);
        assertEquals(this.rp1, structureDelta.getNewValue());
    }

    @Test
    public void structureRemovedProperties() {
        this.scd.addRemoved(PropertyType.CAMPUS, "Hello");
        this.scd.addRemoved(PropertyType.IS_ALTERNATING, true);
        this.scd.addRemoved(PropertyType.CREDITS, 1.234);
        this.scd.addRemoved(PropertyType.REPEATING_PERIOD, this.rp1);

        RemovalDelta delta =
                (RemovalDelta) this.scd.getValueChanges().stream()
                        .filter(x -> x.getPropertyType() == PropertyType.CAMPUS)
                        .findFirst().orElse(null);
        assertEquals("Hello", delta.getOldValue());

        delta = (RemovalDelta) this.scd.getValueChanges().stream()
                .filter(x -> x.getPropertyType() == PropertyType.IS_ALTERNATING)
                .findFirst().orElse(null);
        assertEquals(true, delta.getOldValue());

        delta = (RemovalDelta) this.scd.getValueChanges().stream()
                .filter(x -> x.getPropertyType() == PropertyType.CREDITS)
                .findFirst().orElse(null);
        assertEquals(1.234, delta.getOldValue());

        StructureRemovalDelta structureDelta =
                (StructureRemovalDelta) this.scd.getValueChanges().stream()
                        .filter(x -> x.getPropertyType() == PropertyType.REPEATING_PERIOD)
                        .findFirst().orElse(null);
        assertEquals(this.rp1, structureDelta.getOldValue());
    }

    @Test
    public void substructureChange(){
        this.scd.addSubstructureChange(StructureDelta.of(PropertyType.REPEATING_PERIOD, this.rp1));
        assertEquals("2016/SEM1/TBA/TBA/TBA",
                this.scd.getSubstructureChanges().stream()
                        .findFirst().orElse(null).getIdentifier()
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void recursiveSubstructureChange(){
        this.scd.addSubstructureChange(this.scd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateSubstructureChange() {
        this.scd.addSubstructureChange(StructureDelta.of(PropertyType.REPEATING_PERIOD, this.rp1));
        this.scd.addSubstructureChange(StructureDelta.of(PropertyType.REPEATING_PERIOD, this.rp1));
    }

    @Test
    public void anyChanges(){
        assertFalse(this.scd.hasChanges());
        this.scd.addValueIfChanged(PropertyType.SUPERVISOR, null, "A");
        assertTrue(this.scd.hasChanges());
        assertTrue(this.scd.hasValueChanges());
        assertFalse(this.scd.hasSubstructureChanges());

        this.scd.addSubstructureChange(StructureDelta.of(PropertyType.REPEATING_PERIOD, this.rp1));
        assertTrue(this.scd.hasChanges());
        assertTrue(this.scd.hasValueChanges());
        assertTrue(this.scd.hasSubstructureChanges());

        this.setUp();

        assertFalse(this.scd.hasChanges());
        this.scd.addSubstructureChange(StructureDelta.of(PropertyType.REPEATING_PERIOD, this.rp1));
        assertTrue(this.scd.hasChanges());
        assertFalse(this.scd.hasValueChanges());
        assertTrue(this.scd.hasSubstructureChanges());
    }

    @Test
    public void string() {

        this.scd.addValueIfChanged(PropertyType.SUPERVISOR, null, "My Supervisor");
        this.scd.addValueIfChanged(PropertyType.SUPERVISOR, null, "My Supervisor 2");
        this.scd.addValueIfChanged(PropertyType.NOTE, "My Note", null);
        this.scd.addValueIfChanged(PropertyType.NOTE, null, "My Note 2");
        this.scd.addValueIfChanged(PropertyType.CREDITS, 2.32, 4.343);
        this.scd.addSubstructureChange(StructureDelta.of(PropertyType.SECTION, Section.of("ID")));

        assertEquals("", this.scd.toString());
    }
}