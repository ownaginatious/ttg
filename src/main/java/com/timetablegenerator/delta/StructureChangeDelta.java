package com.timetablegenerator.delta;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class StructureChangeDelta extends Delta {

    private static String I = Settings.getIndent();

    private static class DeltaSort implements Comparator<Delta>{

        private int operationValue(Delta delta){
            if (delta instanceof ValueAdditionDelta || delta instanceof StructureAdditionDelta) {
                return 0;
            } else if (delta instanceof ValueChangeDelta || delta instanceof StructureChangeDelta) {
                return 1;
            }
            return 2;
        }

        @Override
        public int compare(Delta d1, Delta d2) {
            int opDiff = operationValue(d1) - operationValue(d2);
            if (opDiff != 0) {
                return opDiff;
            }
            return d1.getPropertyType().compareTo(d2.getPropertyType());
        }
    }

    private static DeltaSort sorter = new DeltaSort();

    private final Collection<Delta> propertyChangeDeltas = new HashSet<>();
    private final Map<String, StructureChangeDelta> structureChangeDeltas = new HashMap<>();

    @Getter private final String identifier;

    private StructureChangeDelta(PropertyType propertyType, String identifier) {
        super(propertyType);
        this.identifier = identifier;
    }

    public static StructureChangeDelta of(@NonNull PropertyType propertyType, @NonNull Diffable<?> diffable) {
        return new StructureChangeDelta(propertyType, diffable.getDeltaId());
    }

    public StructureChangeDelta addAdded(PropertyType propertyType, @NonNull Diffable<?> value) {
        this.propertyChangeDeltas.add(StructureAdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addAdded(PropertyType propertyType, @NonNull Boolean value) {
        this.propertyChangeDeltas.add(ValueAdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addAdded(PropertyType propertyType, @NonNull Number value) {
        this.propertyChangeDeltas.add(ValueAdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addAdded(PropertyType propertyType, @NonNull String value) {
        this.propertyChangeDeltas.add(ValueAdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addRemoved(PropertyType propertyType, @NonNull Diffable<?> value) {
        this.propertyChangeDeltas.add(StructureRemovalDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addRemoved(PropertyType propertyType, @NonNull Boolean value) {
        this.propertyChangeDeltas.add(ValueRemovalDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addRemoved(PropertyType propertyType, @NonNull Number value) {
        this.propertyChangeDeltas.add(ValueRemovalDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addRemoved(PropertyType propertyType, @NonNull String value) {
        this.propertyChangeDeltas.add(ValueRemovalDelta.of(propertyType, value));
        return this;
    }

    public StructureChangeDelta addValueIfChanged(PropertyType propertyType,
                                                 Boolean oldValue, Boolean newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            if (newValue == null) {
                this.propertyChangeDeltas.add(ValueRemovalDelta.of(propertyType, oldValue));
            } else if (oldValue == null) {
                this.propertyChangeDeltas.add(ValueAdditionDelta.of(propertyType, newValue));
            } else {
                this.propertyChangeDeltas.add(ValueChangeDelta.of(propertyType, newValue, oldValue));
            }
        }
        return this;
    }

    public StructureChangeDelta addValueIfChanged(PropertyType propertyType,
                                                  Number oldValue, Number newValue) {
        if (!Objects.equals(newValue, oldValue)) {
            if (newValue == null) {
                this.propertyChangeDeltas.add(ValueRemovalDelta.of(propertyType, oldValue));
            } else if (oldValue == null) {
                this.propertyChangeDeltas.add(ValueAdditionDelta.of(propertyType, newValue));
            } else {
                this.propertyChangeDeltas.add(ValueChangeDelta.of(propertyType, newValue, oldValue));
            }
        }
        return this;
    }

    public StructureChangeDelta addValueIfChanged(PropertyType propertyType,
                                                  String oldValue, String newValue) {
        if (Objects.equals(newValue, oldValue)) {
            if (newValue == null) {
                this.propertyChangeDeltas.add(ValueRemovalDelta.of(propertyType, oldValue));
            } else if (oldValue == null) {
                this.propertyChangeDeltas.add(ValueAdditionDelta.of(propertyType, newValue));
            } else {
                this.propertyChangeDeltas.add(ValueChangeDelta.of(propertyType, newValue, oldValue));
            }
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

        StringBuilder sb = new StringBuilder();

        sb.append("MODIFIED [").append(this.getPropertyType().getFieldName()).append(']');
        sb.append(" (id = ").append(this.identifier).append(")");

        int[] i = {0};

        if (this.hasValueChanges()) {

            sb.append("\n\n").append(I).append("Property changes:\n");

            this.propertyChangeDeltas.stream().sorted(sorter)
                    .forEach(d -> sb.append('\n')
                            .append(StringUtilities.indent(2, d.toString())));
        }

        i[0] = 0;

        if (this.hasChildStructureChanges()) {

            sb.append("\n\n").append(I).append("Child property changes:");

            this.structureChangeDeltas.values().stream().sorted(sorter)
                    .forEach(d -> sb.append("\n\n")
                            .append(StringUtilities.indent(2, d.toString())));
        }

        return sb.toString();
    }
}