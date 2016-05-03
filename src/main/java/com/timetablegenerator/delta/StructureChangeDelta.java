package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class StructureChangeDelta extends Delta {

    private static class DeltaSort implements Comparator<Delta>{
        @Override
        public int compare(Delta d1, Delta d2) {
            return d1.getPropertyType().compareTo(d2.getPropertyType());
        }
    }

    private static DeltaSort sorter = new DeltaSort();

    private final Collection<Delta> propertyChangeDeltas = new HashSet<>();
    private final Map<String, StructureChangeDelta> structureChangeDeltas = new HashMap<>();

    @Getter private final String identifier;

    public static StructureChangeDelta of(@NonNull PropertyType propertyType, @NonNull Diffable<?> diffable) {
        return new StructureChangeDelta(propertyType, diffable.getDeltaId());
    }

    private StructureChangeDelta(@NonNull PropertyType propertyType, String identifier) {

        super(propertyType);
        this.identifier = identifier;
    }

    public StructureChangeDelta addAdded(PropertyType propertyType, @NonNull Object value) {
        this.propertyChangeDeltas.add(new AdditionDelta(propertyType, value));
        return this;
    }

    public StructureChangeDelta addRemoved(PropertyType propertyType, @NonNull Object value) {
        this.propertyChangeDeltas.add(new RemovalDelta(propertyType, value));
        return this;
    }

    public <T> StructureChangeDelta addIfChanged(PropertyType propertyType, T oldValue, T newValue) {

        if (Objects.equals(newValue, oldValue)) {
            return this;
        }

        if (newValue == null) {
            this.propertyChangeDeltas.add(new RemovalDelta(propertyType, oldValue));
        } else if (oldValue == null) {
            this.propertyChangeDeltas.add(new AdditionDelta(propertyType, newValue));
        } else {
            this.propertyChangeDeltas.add(new ValueChangeDelta(propertyType, newValue, oldValue));
        }

        return this;
    }

    public StructureChangeDelta addChange(StructureChangeDelta delta) {

        String identifier = delta.getIdentifier();

        if (this.structureChangeDeltas.containsKey(identifier)){
            throw new IllegalArgumentException("Change delta already recorded for identifier \'" + identifier + "\'");
        }

        this.structureChangeDeltas.put(identifier, delta);
        return this;
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
        return this.structureChangeDeltas.values();
    }

    public String toString() {
        return this.toString(0);
    }

    public String toString(int tabAmount) {

        final String tabs = generateTabs(tabAmount);

        StringBuilder sb = new StringBuilder();

        sb.append("MODIFIED [").append(this.getPropertyType().name()).append(']');
        sb.append(" (id = ").append(this.identifier).append(")");

        int[] i = {0};

        if (this.hasValueChanges()) {

            sb.append("\n\n").append(tabs).append(tabs).append("Property changes:\n");

            this.propertyChangeDeltas.stream().sorted(sorter)
                    .forEach(d -> sb.append('\n').append(tabs).append(tabs).append(tabs)
                            .append('[').append(++i[0]).append("] ").append(d));
        }

        i[0] = 0;

        if (this.hasChildStructureChanges()) {

            sb.append("\n\n").append(tabs).append(tabs).append("Child property changes:");

            this.structureChangeDeltas.values().stream().sorted(sorter)
                    .forEach(d -> sb.append("\n\n").append(tabs).append(tabs).append(tabs)
                        .append('[').append(++i[0]).append("] ").append(d.toString(tabAmount + 1)));
        }

        return sb.toString();
    }
}