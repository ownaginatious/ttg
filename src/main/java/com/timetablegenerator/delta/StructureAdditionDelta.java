package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class StructureAdditionDelta extends Delta {

    @Getter private final Diffable<?> newValue;

    private StructureAdditionDelta(@Nonnull PropertyType propertyType, @Nonnull Diffable<?> newValue) {
        super(propertyType);
        this.newValue = newValue;
    }

    public static StructureAdditionDelta of(@NonNull PropertyType propertyType, @NonNull Diffable<?> newValue) {
        return new StructureAdditionDelta(propertyType, newValue);
    }

    public String toString() {

        return "ADDED [" + this.getPropertyType().getFieldName() + "] (id = "
                +  this.newValue.getDeltaId() + ")";
    }
}