package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class ValueRemovalDelta extends Delta {

    @Getter private final Object oldValue;

    private ValueRemovalDelta(PropertyType propertyType, Object oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
    }

    public static ValueRemovalDelta of(@NonNull PropertyType propertyType, @NonNull Boolean oldValue) {
        return new ValueRemovalDelta(propertyType, oldValue);
    }

    public static ValueRemovalDelta of(@NonNull PropertyType propertyType, @NonNull String oldValue) {
        return new ValueRemovalDelta(propertyType, oldValue);
    }

    public static ValueRemovalDelta of(@NonNull PropertyType propertyType, @NonNull Number oldValue) {
        return new ValueRemovalDelta(propertyType, oldValue);
    }

    public String toString() {
        return "REMOVED [" + this.getPropertyType().getFieldName() + "] (value = " + this.oldValue + ")";
    }
}