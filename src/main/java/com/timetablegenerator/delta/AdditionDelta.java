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

    public static AdditionDelta of(PropertyType propertyType, Number newValue) {
        return new AdditionDelta(propertyType, newValue);
    }

    public static AdditionDelta of(PropertyType propertyType, Diffable<?> newValue) {
        return new AdditionDelta(propertyType, newValue);
    }

    public String toString() {
        String s = newValue instanceof Diffable ?
                ((Diffable) newValue).getDeltaId() : this.newValue.toString();
        return "ADDED [" + this.getPropertyType().getFieldName() + "] (value = " +  s + ")";
    }

    @Override
    public int compareTo(@Nonnull Delta delta) {
        if (delta instanceof StructureDelta){
            return -1;
        }
        return 0;
    }
}