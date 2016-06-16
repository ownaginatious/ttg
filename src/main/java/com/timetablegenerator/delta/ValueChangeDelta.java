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

    ValueChangeDelta(@NonNull PropertyType propertyType, @NonNull Object oldValue, @NonNull Object newValue) {
        super(propertyType);
        propertyType.validateType(oldValue.getClass());
        propertyType.validateType(newValue.getClass());
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static ValueChangeDelta of(PropertyType propertyType, Boolean oldValue, Boolean newValue){
        return new ValueChangeDelta(propertyType, oldValue, newValue);
    }

    public static ValueChangeDelta of(PropertyType propertyType, String oldValue, String newValue){
        return new ValueChangeDelta(propertyType, oldValue, newValue);
    }

    public static ValueChangeDelta of(PropertyType propertyType, Number oldValue, Number newValue){
        return new ValueChangeDelta(propertyType, oldValue, newValue);
    }

    public String toString() {
        return "MODIFIED [" + this.getPropertyType().getFieldName() + "]\n"
                  + I + "Old value : \"" + oldValue + "\"\n"
                  + I + "New value : \"" + newValue + "\"";
    }
}