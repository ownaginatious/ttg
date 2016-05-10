package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class StructureAdditionDelta extends Delta {

    private final Diffable<?> newValue;

    public StructureAdditionDelta(@Nonnull PropertyType propertyType, @Nonnull Diffable<?> newValue) {

        super(propertyType);
        this.newValue = newValue;
    }

    public String toString() {

        return "ADDED [" + this.getPropertyType().name() + "] (id = "
                +  this.newValue.getDeltaId() + ")";
    }
}