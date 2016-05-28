package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class StructureRemovalDelta extends Delta {

    private final Diffable<?> oldValue;

    public StructureRemovalDelta(@Nonnull PropertyType propertyType, @Nonnull Diffable<?> oldValue) {

        super(propertyType);
        this.oldValue = oldValue;
    }

    public String toString() {

        return "REMOVED [" + this.getPropertyType().name() + "] (id = "
                + this.oldValue.getDeltaId() + ")";
    }
}