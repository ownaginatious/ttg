package com.timetablegenerator.model;

import lombok.*;
import javax.annotation.Nonnull;

@EqualsAndHashCode()
@RequiredArgsConstructor(staticName = "of")
public class Department implements Comparable<Department> {

    @NonNull @Getter private final String code;
    @NonNull @Getter private final String name;

    @Override
    public int compareTo(@Nonnull Department that) {
        if (!this.code.equals(that.code)) {
            return this.code.compareTo(that.code);
        }
        if (!this.name.equals(that.name)) {
            return this.name.compareTo(that.name);
        }
        // Some standard classes will use this instead of .equals() so ensure non-zero if not equal.
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.code + ")";
    }
}