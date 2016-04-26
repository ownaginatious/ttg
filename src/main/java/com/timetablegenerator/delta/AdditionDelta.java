package com.timetablegenerator.delta;

import javax.annotation.Nonnull;

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

        return "ADDED [" + this.propertyType.name() + "] : "
                + fixPrinting(TAB + generateTabs(tabAmount), this.newValue);
    }
}