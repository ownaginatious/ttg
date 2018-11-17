package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

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

    public static RemovalDelta of(PropertyType propertyType, Double oldValue) {
        return new RemovalDelta(propertyType, oldValue);
    }

    public static RemovalDelta of(PropertyType propertyType, Integer oldValue) {
        return new RemovalDelta(propertyType, oldValue);
    }

    public static RemovalDelta of(PropertyType propertyType, Diffable<?> oldValue) {
        return new RemovalDelta(propertyType, oldValue);
    }

    public String toString() {
        String s = this.oldValue instanceof Diffable ?
                "id = " + ((Diffable) this.oldValue).getDeltaId() :
                "value = " + this.oldValue.toString();
        return "REMOVED [" + this.getPropertyType().getFieldName() + "] (" + s + ")";
    }

    @Override
    public int compareTo(@Nonnull Delta that) {

        if (that instanceof AdditionDelta) {
            return 1;
        }
        if (!(that instanceof RemovalDelta)) {
            return -1;
        }

        RemovalDelta thatSame = (RemovalDelta) that;

        if (this.getPropertyType() != thatSame.getPropertyType()) {
            return this.getPropertyType().compareTo(thatSame.getPropertyType());
        }
        return this.oldValue.toString().compareTo(thatSame.oldValue.toString());
    }
}