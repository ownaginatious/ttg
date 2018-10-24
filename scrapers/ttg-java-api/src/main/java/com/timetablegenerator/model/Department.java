package com.timetablegenerator.model;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Department implements Comparable<Department> {

    private final String code;
    private final String name;

    public Department(@Nonnull String code, @Nonnull String name) {

        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Department that = (Department) o;

        return Objects.equals(this.code, that.code)
                && Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {

        int result = code.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.code + ")";
    }

    @Override
    public int compareTo(@Nonnull Department d) {
        return this.name.compareTo(d.name);
    }
}