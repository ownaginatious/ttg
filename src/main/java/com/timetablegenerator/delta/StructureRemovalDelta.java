package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class StructureRemovalDelta extends Delta {

    @Getter private final Diffable<?> oldValue;

    private StructureRemovalDelta(PropertyType propertyType, Diffable<?> oldValue) {
        super(propertyType);
        this.oldValue = oldValue;
    }

    public static StructureRemovalDelta of(@NonNull PropertyType propertyType, @NonNull Diffable<?> oldValue){
        return new StructureRemovalDelta(propertyType, oldValue);
    }

    public String toString() {
        return "REMOVED [" + this.getPropertyType().getFieldName() + "] (id = "
                + this.oldValue.getDeltaId() + ")";
    }
}