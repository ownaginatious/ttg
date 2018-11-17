package com.timetablegenerator.delta;

import com.timetablegenerator.Settings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = true)
public class ReplaceDelta extends Delta {

    private static final String I = Settings.getIndent();

    @Getter private final Object oldValue;
    @Getter private final Object newValue;

    ReplaceDelta(@NonNull PropertyType propertyType, @NonNull Object oldValue, @NonNull Object newValue) {
        super(propertyType);
        propertyType.validateType(oldValue.getClass());
        propertyType.validateType(newValue.getClass());
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static ReplaceDelta of(PropertyType propertyType, Boolean oldValue, Boolean newValue){
        return new ReplaceDelta(propertyType, oldValue, newValue);
    }

    public static ReplaceDelta of(PropertyType propertyType, String oldValue, String newValue){
        return new ReplaceDelta(propertyType, oldValue, newValue);
    }

    public static ReplaceDelta of(PropertyType propertyType, Number oldValue, Number newValue){
        return new ReplaceDelta(propertyType, oldValue, newValue);
    }

    public String toString() {
        return "REPLACED [" + this.getPropertyType().getFieldName() + "]\n"
                  + I + "Old value : \"" + oldValue + "\"\n"
                  + I + "New value : \"" + newValue + "\"";
    }

    @Override
    public int compareTo(@Nonnull Delta that) {
        if (that instanceof RemovalDelta || that instanceof AdditionDelta) {
            return 1;
        }
        if (!(that instanceof ReplaceDelta)) {
            return -1;
        }
        if (this.getPropertyType() != that.getPropertyType()) {
            return this.getPropertyType().compareTo(that.getPropertyType());
        }
        int newValueCompare = this.newValue.toString().compareTo(((ReplaceDelta) that).newValue.toString());
        if (newValueCompare != 0) {
            return newValueCompare;
        }
        return this.oldValue.toString().compareTo(((ReplaceDelta) that).oldValue.toString());
    }
}