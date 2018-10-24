package com.timetablegenerator.delta;

import javax.annotation.Nonnull;

public class RemovalDelta extends Delta {

    private final Object oldValue;

    public RemovalDelta(@Nonnull PropertyType propertyType, @Nonnull Object oldValue) {

        super(propertyType);
        this.oldValue = oldValue;
    }

    public String toString() {
        return this.toString(0);
    }

    public String toString(int tabAmount) {

        return "REMOVED [" + this.propertyType.name() + "] : "
                + fixPrinting(TAB + generateTabs(tabAmount), this.oldValue);
    }
}