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
    public int compareTo(@Nonnull Department that) {
        if (!this.code.equals(that.code)) {
            return this.code.compareTo(that.code);
        }
        if (!this.name.equals(that.name)) {
            return this.name.compareTo(that.name);
        }
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.code + ")";
    }
}