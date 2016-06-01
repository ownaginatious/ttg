package com.timetablegenerator.model;

import lombok.*;
import javax.annotation.Nonnull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode()
public class Department implements Comparable<Department> {

    @Getter private final String code;
    @Getter private final String name;

    public static Department of(@NonNull String code, @NonNull String name) {
        return new Department(code, name);
    }

    @Override
    public int compareTo(@Nonnull Department d) {
        return this.name.compareTo(d.name);
    }
    
    @Override
    public String toString() {
        return this.name + " (" + this.code + ")";
    }
}