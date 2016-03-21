package com.timetablegenerator.delta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class StructureChangeDelta extends Delta {

    private final Collection<Delta> propertyChangeDeltas = new ArrayList<>();
    private final Collection<StructureChangeDelta> structureChangeDeltas = new ArrayList<>();

    private final String identifier;

    public static StructureChangeDelta of(@Nonnull PropertyType propertyType) {
        return new StructureChangeDelta(propertyType, null);
    }

    public static StructureChangeDelta of(@Nonnull PropertyType propertyType, @Nonnull String identifier) {
        return new StructureChangeDelta(propertyType, identifier);
    }

    private StructureChangeDelta(@Nonnull PropertyType propertyType, String identifier) {

        super(propertyType);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void addAdded(PropertyType propertyType, @Nonnull Object value) {
        this.propertyChangeDeltas.add(new AdditionDelta(propertyType, value));
    }

    public void addRemoved(PropertyType propertyType, @Nonnull Object value) {
        this.propertyChangeDeltas.add(new RemovalDelta(propertyType, value));
    }

    public <T> void addIfChanged(PropertyType propertyType, T oldValue, T newValue) {

        if (Objects.equals(newValue, oldValue))
            return;

        if (newValue == null)
            this.propertyChangeDeltas.add(new RemovalDelta(propertyType, oldValue));
        else if (oldValue == null)
            this.propertyChangeDeltas.add(new AdditionDelta(propertyType, newValue));
        else
            this.propertyChangeDeltas.add(new ValueChangeDelta(propertyType, newValue, oldValue));
    }

    public void addChange(StructureChangeDelta delta) {
        this.structureChangeDeltas.add(delta);
    }

    public boolean hasValueChanges() {
        return !this.propertyChangeDeltas.isEmpty();
    }

    public boolean hasChildStructureChanges() {
        return !this.structureChangeDeltas.isEmpty();
    }

    public boolean hasChanges() {
        return this.hasValueChanges() || this.hasChildStructureChanges();
    }

    public Collection<Delta> getValueChanges() {
        return this.propertyChangeDeltas;
    }

    public Collection<StructureChangeDelta> getChildChanges() {
        return this.structureChangeDeltas;
    }

    public String toString() {
        return this.toString(0);
    }

    public String toString(int tabAmount) {

        final String tabs = generateTabs(tabAmount);

        StringBuilder sb = new StringBuilder();

        sb.append(tabs).append("MODIFIED [").append(this.propertyType.name()).append("] (id = ")
                .append(this.identifier).append(")\n\n");

        if (this.hasValueChanges()) {

            sb.append(tabs).append(tabs).append("Property changes:\n\n");

            int i = 0;

            for (Delta pd : this.propertyChangeDeltas) {

                sb.append(tabs).append(tabs).append(tabs)
                        .append('[').append(++i).append("] ").append(pd).append('\n');
            }
        }

        if (this.hasChildStructureChanges()) {

            sb.append(tabs).append(tabs).append("Child property changes:\n\n");

            int i = 0;

            for (StructureChangeDelta pd : this.structureChangeDeltas) {

                sb.append(tabs).append(tabs).append(tabs)
                        .append('[').append(++i).append("] \n").append(pd.toString(tabAmount + 4)).append('\n');
            }
        }

        return sb.toString();
    }
}