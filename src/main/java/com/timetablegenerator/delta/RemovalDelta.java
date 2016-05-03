package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
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

        return "REMOVED [" + this.getPropertyType().name() + "] : "
                + fixPrinting(TAB + generateTabs(tabAmount), this.oldValue);
    }
}