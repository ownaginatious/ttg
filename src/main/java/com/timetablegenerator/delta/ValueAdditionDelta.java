package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class ValueAdditionDelta extends Delta {

    private final Object newValue;

    public ValueAdditionDelta(@Nonnull PropertyType propertyType, @Nonnull String newValue) {
        super(propertyType);
        this.newValue = newValue;
    }

    public ValueAdditionDelta(@Nonnull PropertyType propertyType, @Nonnull Boolean newValue) {
        super(propertyType);
        this.newValue = newValue;
    }

    public ValueAdditionDelta(@Nonnull PropertyType propertyType, @Nonnull Number newValue) {
        super(propertyType);
        this.newValue = newValue;
    }

    public String toString() {
        return "ADDED [" + this.getPropertyType().name() + "] (value = " +  this.newValue.toString() + ")";
    }
}