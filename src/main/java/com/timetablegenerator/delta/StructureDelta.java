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

    public StructureDelta addAdded(PropertyType propertyType, @NonNull Diffable<?> value) {
        this.valueDeltas.add(AdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull Boolean value) {
        this.valueDeltas.add(AdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull Number value) {
        this.valueDeltas.add(AdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureDelta addAdded(PropertyType propertyType, @NonNull String value) {
        this.valueDeltas.add(AdditionDelta.of(propertyType, value));
        return this;
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull Boolean value) {
        this.valueDeltas.add(RemovalDelta.of(propertyType, value));
        return this;
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull Number value) {
        this.valueDeltas.add(RemovalDelta.of(propertyType, value));
        return this;
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull String value) {
        this.valueDeltas.add(RemovalDelta.of(propertyType, value));
        return this;
    }

    public StructureDelta addRemoved(PropertyType propertyType, @NonNull Diffable<?> value) {
        this.valueDeltas.add(RemovalDelta.of(propertyType, value));
        return this;
    }

    private StructureDelta addValueIfChanged(PropertyType propertyType,
                                             Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            if (newValue == null) {
                this.valueDeltas.add(new RemovalDelta(propertyType, oldValue));
            } else if (oldValue == null) {
                this.valueDeltas.add(new AdditionDelta(propertyType, newValue));
            } else {
                propertyType.validateType(oldValue.getClass());
                propertyType.validateType(newValue.getClass());
                this.valueDeltas.add(new ValueChangeDelta(propertyType, newValue, oldValue));
            }
        }
        return this;
    }

    public StructureDelta addValueIfChanged(PropertyType propertyType,
                                            Boolean oldValue, Boolean newValue) {
        return this.addValueIfChanged(propertyType, (Object) oldValue, newValue);
    }

    public StructureDelta addValueIfChanged(PropertyType propertyType,
                                            Number oldValue, Number newValue) {
        return this.addValueIfChanged(propertyType, (Object) oldValue, newValue);
    }

    public StructureDelta addValueIfChanged(PropertyType propertyType,
                                            String oldValue, String newValue) {
        return this.addValueIfChanged(propertyType, (Object) oldValue, newValue);
    }

    public <T> StructureDelta addValueIfChanged(PropertyType propertyType,
                                            Diffable<T> oldValue, Diffable<T> newValue) {
        if (oldValue != null && newValue == null) {
            this.valueDeltas.add(new RemovalDelta(propertyType, oldValue));
        } else if (oldValue == null && newValue != null) {
            this.valueDeltas.add(new AdditionDelta(propertyType, newValue));
        } else if (oldValue != null) {

            propertyType.validateType(oldValue.getClass());
            propertyType.validateType(newValue.getClass());

            StructureDelta delta = oldValue.findDifferences(newValue);
            if (delta.hasChanges()) {
                this.substructureDeltas.add(delta);
            }
        }
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
                    .forEach(d -> sb.append('\n')
                            .append(StringUtilities.indent(2, d.toString())));
        }

        if (this.hasSubstructureChanges()) {

            sb.append("\n\n").append(I).append("Substructure changes:");

            this.substructureDeltas.stream().sorted()
                    .forEach(d -> sb.append("\n\n")
                            .append(StringUtilities.indent(2, d.toString())));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull Delta delta) {
        if (!(delta instanceof StructureDelta)){
            return -1;
        }
        return 0;
    }
}