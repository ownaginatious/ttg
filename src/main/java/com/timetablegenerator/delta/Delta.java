package com.timetablegenerator.delta;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

/**
 * A class used to record the property changes of major data type within a time table
 * (e.g. timetable, period. etc).
 *
 */
@EqualsAndHashCode
public abstract class Delta implements Comparable<Delta> {

    @Getter private final PropertyType propertyType;

    public Delta(@NonNull PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    @Override
    public final int compareTo(@NonNull @Nonnull Delta that){
        return this.propertyType.compareTo(that.propertyType);
    }
}