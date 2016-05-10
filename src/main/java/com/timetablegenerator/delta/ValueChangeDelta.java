package com.timetablegenerator.delta;

import com.timetablegenerator.Settings;
import lombok.EqualsAndHashCode;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class ValueChangeDelta extends Delta {

    private static final String I = Settings.getIndent();

    private final Object oldValue;
    private final Object newValue;

    ValueChangeDelta(PropertyType propertyType, @Nonnull Boolean newValue, @Nonnull Boolean oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    ValueChangeDelta(PropertyType propertyType, @Nonnull Number newValue, @Nonnull Number oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    ValueChangeDelta(PropertyType propertyType, @Nonnull String newValue, @Nonnull String oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String toString() {

        return "MODIFIED [" + this.getPropertyType().name() + "]\n"
                  + I + "Old value : \"" + oldValue + "\"\n"
                  + I + "New value : \"" + newValue + "\"";
    }
}