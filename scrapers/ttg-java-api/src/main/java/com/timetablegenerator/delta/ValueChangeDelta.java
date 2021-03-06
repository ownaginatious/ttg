package com.timetablegenerator.delta;

import javax.annotation.Nonnull;

public class ValueChangeDelta extends Delta {

    private final Object oldValue;
    private final Object newValue;

    public <T> ValueChangeDelta(PropertyType propertyType, @Nonnull T newValue, @Nonnull T oldValue) {

        super(propertyType);

        this.oldValue = newValue;
        this.newValue = oldValue;
    }

    public String toString() {
        return this.toString(0);
    }

    public String toString(int tabAmount) {

        final String tabs = Delta.generateTabs(tabAmount);

        return tabs + "MODIFIED [" + this.propertyType.name() + "]\n"
                  + tabs + Delta.TAB + "Old value : \"" + oldValue + "\"\n"
                  + tabs + Delta.TAB + "New value : \"" + newValue + "\"";
    }
}