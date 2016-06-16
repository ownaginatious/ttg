package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = true)
public class RemovalDelta extends Delta {

    @Getter private final Object oldValue;

    RemovalDelta(@NonNull PropertyType propertyType, @NonNull Object oldValue) {
        super(propertyType);
        propertyType.validateType(oldValue.getClass());
        this.oldValue = oldValue;
    }

    public static RemovalDelta of(PropertyType propertyType, Boolean oldValue) {
        return new RemovalDelta(propertyType, oldValue);
    }

    public static RemovalDelta of(PropertyType propertyType, String oldValue) {
        return new RemovalDelta(propertyType, oldValue);
    }

    public static RemovalDelta of(PropertyType propertyType, Number oldValue) {
        return new RemovalDelta(propertyType, oldValue);
    }

    public static RemovalDelta of(PropertyType propertyType, Diffable<?> oldValue) {
        return new RemovalDelta(propertyType, oldValue);
    }

    public String toString() {
        String s = this.oldValue instanceof Diffable ?
                ((Diffable) this.oldValue).getDeltaId() : this.oldValue.toString();
        return "REMOVED [" + this.getPropertyType().getFieldName() + "] (value = " +  s + ")";
    }
}