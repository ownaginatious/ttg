package com.timetablegenerator.delta;

import com.timetablegenerator.Settings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class ValueChangeDelta extends Delta {

    private static final String I = Settings.getIndent();

    @Getter private final Object oldValue;
    @Getter private final Object newValue;

    private ValueChangeDelta(PropertyType propertyType, Object oldValue, Object newValue) {
        super(propertyType);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static ValueChangeDelta of(PropertyType propertyType, @NonNull Boolean oldValue, @NonNull Boolean newValue){
        return new ValueChangeDelta(propertyType, oldValue, newValue);
    }

    public static ValueChangeDelta of(PropertyType propertyType, @NonNull String oldValue, @NonNull String newValue){
        return new ValueChangeDelta(propertyType, oldValue, newValue);
    }

    public static ValueChangeDelta of(PropertyType propertyType, @NonNull Number oldValue, @NonNull Number newValue){
        return new ValueChangeDelta(propertyType, oldValue, newValue);
    }

    public String toString() {
        return "MODIFIED [" + this.getPropertyType().getFieldName() + "]\n"
                  + I + "Old value : \"" + oldValue + "\"\n"
                  + I + "New value : \"" + newValue + "\"";
    }
}