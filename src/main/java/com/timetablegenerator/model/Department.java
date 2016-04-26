package com.timetablegenerator.model;

import lombok.*;
import javax.annotation.Nonnull;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@EqualsAndHashCode()
public class Department implements Comparable<Department> {

    @Getter @NonNull private final String code;
    @Getter @NonNull private final String name;

    @Override
    public int compareTo(@Nonnull Department d) {
        return this.name.compareTo(d.name);
    }
    
    @Override
    public String toString() {
        return this.name + " (" + this.code + ")";
    }
}