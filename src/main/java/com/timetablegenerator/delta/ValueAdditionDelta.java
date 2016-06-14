package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class ValueAdditionDelta extends Delta {

    @Getter private final Object newValue;

    private ValueAdditionDelta(PropertyType propertyType, Object newValue) {
        super(propertyType);
        this.newValue = newValue;
    }

    public static ValueAdditionDelta of(@NonNull PropertyType propertyType, @NonNull String newValue) {
        return new ValueAdditionDelta(propertyType, newValue);
    }

    public static ValueAdditionDelta of(@NonNull PropertyType propertyType, @NonNull Boolean newValue) {
        return new ValueAdditionDelta(propertyType, newValue);
    }

    public static ValueAdditionDelta of(@NonNull PropertyType propertyType, @NonNull Number newValue) {
        return new ValueAdditionDelta(propertyType, newValue);
    }

    public String toString() {
        return "ADDED [" + this.getPropertyType().getFieldName() + "] (value = " +  this.newValue.toString() + ")";
    }
}