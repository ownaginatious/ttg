package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class AdditionDelta extends Delta {

    private final Object newValue;

    public AdditionDelta(@Nonnull PropertyType propertyType, @Nonnull Object newValue) {

        super(propertyType);
        this.newValue = newValue;
    }

    public String toString() {
        return this.toString(0);
    }

    public String toString(int tabAmount) {

        return "ADDED [" + this.getPropertyType().name() + "] : "
                + fixPrinting(TAB + generateTabs(tabAmount), this.newValue);
    }
}