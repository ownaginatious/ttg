package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
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

        final String tabs = generateTabs(tabAmount);

        return tabs + "MODIFIED [" + this.getPropertyType().name() + "]\n"
                  + tabs + TAB + "Old value : \"" + oldValue + "\"\n"
                  + tabs + TAB + "New value : \"" + newValue + "\"";
    }
}