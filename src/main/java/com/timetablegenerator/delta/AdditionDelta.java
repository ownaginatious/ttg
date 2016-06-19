package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class AdditionDelta extends Delta {

    @Getter private final Object newValue;

    AdditionDelta(@NonNull PropertyType propertyType, @NonNull Object newValue) {
        super(propertyType);
        propertyType.validateType(newValue.getClass());
        this.newValue = newValue;
    }

    public static AdditionDelta of(PropertyType propertyType, String newValue) {
        return new AdditionDelta(propertyType, newValue);
    }

    public static AdditionDelta of(PropertyType propertyType, Boolean newValue) {
        return new AdditionDelta(propertyType, newValue);
    }

    public static AdditionDelta of(PropertyType propertyType, Double newValue) {
        return new AdditionDelta(propertyType, newValue);
    }

    public static AdditionDelta of(PropertyType propertyType, Integer newValue) {
        return new AdditionDelta(propertyType, newValue);
    }

    public static AdditionDelta of(PropertyType propertyType, Diffable<?> newValue) {
        return new AdditionDelta(propertyType, newValue);
    }

    public String toString() {
        String s = this.newValue instanceof Diffable ?
                "id = " + ((Diffable) this.newValue).getDeltaId() :
                "value = " + this.newValue.toString();
        return "ADDED [" + this.getPropertyType().getFieldName() + "] (" + s + ")";
    }

    @Override
    public int compareTo(@Nonnull Delta that) {
        if (that instanceof RemovalDelta) {
            return -1;
        }
        if (this.getPropertyType() != that.getPropertyType()) {
            return this.getPropertyType().compareTo(that.getPropertyType());
        }
        // Only way to ensure a stable reproducible ordering.
        return this.newValue.hashCode() - ((AdditionDelta) that).newValue.hashCode();
    }
}