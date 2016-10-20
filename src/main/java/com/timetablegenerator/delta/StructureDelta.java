package com.timetablegenerator.delta;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
public class StructureDelta extends Delta {

    private static String I = Settings.getIndent();

    private final Collection<Delta> valueDeltas = new HashSet<>();
    private final Collection<StructureDelta> substructureDeltas = new HashSet<>();

    @Getter private final String identifier;

    private StructureDelta(PropertyType propertyType, String identifier) {
        super(propertyType);
        this.identifier = identifier;
    }

    public static StructureDelta of(@NonNull PropertyType propertyType, @NonNull Diffable<?> diffable) {
        propertyType.validateType(diffable.getClass());
        return new StructureDelta(propertyType, diffable.getDeltaId());
    }

    private StructureDelta addValueDelta(Delta delta){

        PropertyType propertyType = delta.getPropertyType();

        // Check to ensure we are not producing an ambiguity.
        if (propertyType.isSingular()) {
            for (Delta existing : this.valueDeltas) {
                if (existing.getPropertyType() == propertyType) {
                    throw new IllegalArgumentException(
                            String.format("Value delta of type \"%s\" is defined multiple times", propertyType));
                }
            }
        }
        this.valueDeltas.add(delta);
        return this;
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull Diffable<?> value) {
        return this.addValueDelta(AdditionDelta.of(propertyType, value));
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull Boolean value) {
        return this.addValueDelta(AdditionDelta.of(propertyType, value));
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull Double value) {
        return this.addValueDelta(AdditionDelta.of(propertyType, value));
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull Integer value) {
        return this.addValueDelta(AdditionDelta.of(propertyType, value));
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull String value) {
        return this.addValueDelta(AdditionDelta.of(propertyType, value));
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull Boolean value) {
        return this.addValueDelta(RemovalDelta.of(propertyType, value));
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull Double value) {
        return this.addValueDelta(RemovalDelta.of(propertyType, value));
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull Integer value) {
        return this.addValueDelta(RemovalDelta.of(propertyType, value));
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull String value) {
        return this.addValueDelta(RemovalDelta.of(propertyType, value));
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull Diffable<?> value) {
        return this.addValueDelta(RemovalDelta.of(propertyType, value));
    }

    private <T> StructureDelta addValueIfChangedInternal(PropertyType propertyType,
                                                         Comparable<T> oldValue, Comparable<T> newValue) {
        Delta newDelta = null;
        if (!Objects.equals(oldValue, newValue)) {
            if (newValue == null) {
                newDelta = new RemovalDelta(propertyType, oldValue);
            } else if (oldValue == null) {
                newDelta = new AdditionDelta(propertyType, newValue);
            } else {
                propertyType.validateType(oldValue.getClass());
                propertyType.validateType(newValue.getClass());
                newDelta = new ReplaceDelta(propertyType, newValue, oldValue);
            }
        }
        if (newDelta != null) {
            this.addValueDelta(newDelta);
        }
        return this;
    }

    public StructureDelta addValueIfChanged(PropertyType propertyType,
                                            Boolean oldValue, Boolean newValue) {
        return this.addValueIfChangedInternal(propertyType, oldValue, newValue);
    }

    public StructureDelta addValueIfChanged(PropertyType propertyType,
                                            Integer oldValue, Integer newValue) {
        return this.addValueIfChangedInternal(propertyType, oldValue, newValue);
    }

    public StructureDelta addValueIfChanged(PropertyType propertyType,
                                            Double oldValue, Double newValue) {
        return this.addValueIfChangedInternal(propertyType, oldValue, newValue);
    }

    public StructureDelta addValueIfChanged(PropertyType propertyType,
                                            String oldValue, String newValue) {
        return this.addValueIfChangedInternal(propertyType, oldValue, newValue);
    }

    public StructureDelta addSubstructureChange(@NonNull StructureDelta delta){

        PropertyType propertyType = delta.getPropertyType();
        String id = delta.getIdentifier();

        for (StructureDelta existing : this.substructureDeltas) {
            if (existing.getPropertyType() == propertyType && existing.getIdentifier().equals(id)){
                throw new IllegalArgumentException(
                        String.format("Substructure change of type \"%s\" for ID \"%s\" is defined multiple times",
                                propertyType, id));
            }
        }
        this.substructureDeltas.add(delta);
        return this;
    }

    public boolean hasValueChanges() {
        return !this.valueDeltas.isEmpty();
    }

    public boolean hasSubstructureChanges() {
        return !this.substructureDeltas.isEmpty();
    }

    public boolean hasChanges() {
        return this.hasValueChanges() || this.hasSubstructureChanges();
    }

    public Collection<Delta> getValueChanges() {
        return Collections.unmodifiableCollection(this.valueDeltas);
    }

    public Collection<StructureDelta> getSubstructureChanges() {
        return Collections.unmodifiableCollection(this.substructureDeltas);
    }

    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("MODIFIED [").append(this.getPropertyType().getFieldName()).append(']');
        sb.append(" (id = ").append(this.identifier).append(")");

        if (this.hasValueChanges()) {

            sb.append("\n\n").append(I).append("Value changes:\n");

            this.valueDeltas.stream().sorted()
                    .forEach(d -> sb.append('\n').append(StringUtilities.indent(2, d.toString())));
        }

        if (this.hasSubstructureChanges()) {

            sb.append("\n\n").append(I).append("Substructure changes:");

            this.substructureDeltas.stream().sorted()
                    .forEach(d -> sb.append("\n\n").append(StringUtilities.indent(2, d.toString())));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull Delta delta) {
        if (!(delta instanceof StructureDelta)){
            return 1;
        }
        StructureDelta that = (StructureDelta) delta;
        if (this.getPropertyType() != that.getPropertyType()) {
            return this.getPropertyType().compareTo(that.getPropertyType());
        }
        return this.getIdentifier().compareTo(that.getIdentifier());
    }
}