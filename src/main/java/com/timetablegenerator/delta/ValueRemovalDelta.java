package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class ValueRemovalDelta extends Delta {

    private final Object oldValue;

    public ValueRemovalDelta(@Nonnull PropertyType propertyType, @Nonnull String oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
    }

    public ValueRemovalDelta(@Nonnull PropertyType propertyType, @Nonnull Boolean oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
    }

    public ValueRemovalDelta(@Nonnull PropertyType propertyType, @Nonnull Number oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
    }

    public String toString() {
        return "REMOVED [" + this.getPropertyType().name() + "] (value = " + this.oldValue + ")";
    }
}