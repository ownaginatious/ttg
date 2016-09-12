package com.timetablegenerator.tests.api.diff;

import com.timetablegenerator.delta.*;
import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class StructuralDeltaTests {

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
        AdditionDelta ad = AdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals(PropertyType.REPEATING_PERIOD, ad.getPropertyType());
        assertEquals(this.rp1, ad.getNewValue());
    }

    @Test
    public void additionString() {
        AdditionDelta ad = AdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals("ADDED [REPEATING_PERIOD] (id = 2016/SEM1/TBA/TBA/TBA)", ad.toString());
    }

    @Test
    public void additionEquality(){
        AdditionDelta ad = AdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        AdditionDelta ad1 = AdditionDelta.of(PropertyType.ONE_TIME_PERIOD, OneTimePeriod.of(this.term));
        AdditionDelta ad2 = AdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp2);
        AdditionDelta ad3 = AdditionDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);

        assertEquals(ad, ad3);
        assertNotEquals(ad, ad1);
        assertNotEquals(ad, ad2);
    }

    @Test
    public void removal() {
        RemovalDelta rd = RemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals(PropertyType.REPEATING_PERIOD, rd.getPropertyType());
        assertEquals(this.rp1, rd.getOldValue());
    }

    @Test
    public void removalString() {
        RemovalDelta rd = RemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        assertEquals("REMOVED [REPEATING_PERIOD] (id = 2016/SEM1/TBA/TBA/TBA)", rd.toString());
    }

    @Test
    public void removalEquality(){
        RemovalDelta rd = RemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);
        RemovalDelta rd1 = RemovalDelta.of(PropertyType.ONE_TIME_PERIOD, OneTimePeriod.of(this.term));
        RemovalDelta rd2 = RemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp2);
        RemovalDelta rd3 = RemovalDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);

        assertEquals(rd, rd3);
        assertNotEquals(rd, rd1);
        assertNotEquals(rd, rd2);
    }

    @Test
    public void numericPropertyChanges() {

        this.scd.hasValueChanges();
        this.scd.addValueIfChanged(PropertyType.CREDITS, 1.0, 1.3);
        assertEquals(1, this.scd.getValueChanges().size());
        assertEquals(ReplaceDelta.class, this.scd.getValueChanges().stream()
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
        assertEquals(ReplaceDelta.class, this.scd.getValueChanges().stream()
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
        assertEquals(ReplaceDelta.class, this.scd.getValueChanges().stream()
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

        AdditionDelta structureDelta =
                (AdditionDelta) this.scd.getValueChanges().stream()
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

        RemovalDelta structureDelta =
                (RemovalDelta) this.scd.getValueChanges().stream()
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

    @Test(expected = IllegalArgumentException.class)
    public void ambiguousValueDiff() {
        this.scd.addValueIfChanged(PropertyType.NAME, "TestA", "TestC");
        this.scd.addValueIfChanged(PropertyType.NAME, "TestB", "TestC");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ambiguousSubstructureDiff() {
        this.scd.addSubstructureChange(
                StructureDelta.of(PropertyType.SECTION, Section.of("ID")).addRemoved(PropertyType.NOTE, "B")
        );
        this.scd.addSubstructureChange(
                StructureDelta.of(PropertyType.SECTION, Section.of("ID")).addRemoved(PropertyType.NOTE, "A")
        );
    }

    @Test
    public void string() {

        this.scd.addValueIfChanged(PropertyType.SUPERVISOR, null, "My Supervisor");
        this.scd.addValueIfChanged(PropertyType.SUPERVISOR, null, "My Supervisor 2");
        this.scd.addValueIfChanged(PropertyType.NOTE, "My Note", null);
        this.scd.addValueIfChanged(PropertyType.NOTE, null, "My Note 2");
        this.scd.addValueIfChanged(PropertyType.CREDITS, 2.32, 4.343);
        this.scd.addSubstructureChange(
                StructureDelta.of(PropertyType.SECTION, Section.of("ID3"))
                    .addRemoved(PropertyType.NOTE, "2. This is quite the note!")
                    .addRemoved(PropertyType.NOTE, "1. This is a second note!")
        );
        this.scd.addSubstructureChange(
                StructureDelta.of(PropertyType.SECTION, Section.of("ID1"))
                        .addRemoved(PropertyType.NUM_ENROLLED, 43)
        );
        this.scd.addSubstructureChange(
                StructureDelta.of(PropertyType.SECTION, Section.of("ID2"))
                        .addRemoved(PropertyType.NOTE, "Hello")
        );

        String expected =
                "MODIFIED [REPEATING_PERIOD] (id = 2016/SEM1/TBA/TBA/TBA)\n" +
                "\n" +
                "    Value changes:\n" +
                "\n" +
                "        ADDED [NOTE] (value = My Note 2)\n" +
                "        ADDED [SUPERVISOR] (value = My Supervisor)\n" +
                "        ADDED [SUPERVISOR] (value = My Supervisor 2)\n" +
                "        REMOVED [NOTE] (value = My Note)\n" +
                "        REPLACED [CREDITS]\n" +
                "            Old value : \"4.343\"\n" +
                "            New value : \"2.32\"\n" +
                "\n" +
                "    Substructure changes:\n" +
                "\n" +
                "        MODIFIED [SECTION] (id = ID1)\n" +
                "        \n" +
                "            Value changes:\n" +
                "        \n" +
                "                REMOVED [NUM_ENROLLED] (value = 43)\n" +
                "\n" +
                "        MODIFIED [SECTION] (id = ID2)\n" +
                "        \n" +
                "            Value changes:\n" +
                "        \n" +
                "                REMOVED [NOTE] (value = Hello)\n" +
                "\n" +
                "        MODIFIED [SECTION] (id = ID3)\n" +
                "        \n" +
                "            Value changes:\n" +
                "        \n" +
                "                REMOVED [NOTE] (value = 1. This is a second note!)\n" +
                "                REMOVED [NOTE] (value = 2. This is quite the note!)";

        assertEquals(expected, this.scd.toString());
    }

    @Test
    public void structureDeltaCompare() {

        StructureDelta scd1 = StructureDelta.of(PropertyType.SECTION, Section.of("ABC"));
        StructureDelta scd2 = StructureDelta.of(PropertyType.REPEATING_PERIOD, this.rp1);

        assertThat(scd1.compareTo(scd2), lessThan(0));
        assertThat(scd2.compareTo(scd1), greaterThan(0));

        AdditionDelta ad = AdditionDelta.of(PropertyType.CREDITS, 1.0);

        assertThat(scd1.compareTo(ad), greaterThan(0));
        assertThat(ad.compareTo(scd1), lessThan(0));
    }
}