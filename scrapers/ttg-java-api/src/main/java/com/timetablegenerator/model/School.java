package com.timetablegenerator.model;

import javax.annotation.Nonnull;
import java.util.*;

public class School {

    private final String schoolName;
    private final String schoolId;

    private final boolean useDepartmentPrefixes;

    private final Map<String, String> sectionTypeNameByCode = new HashMap<>();
    private final Map<String, String> sectionTypeCodeByName = new HashMap<>();

    public static class Builder {

        private final String name;
        private final String id;
        private boolean usesDepartmentPrefixes = false;
        private final Map<String, String> sections = new HashMap<>();
        private final Set<String> sectionNames = new HashSet<>();
        private final Set<String> sectionCodes = new HashSet<>();

        private Builder(@Nonnull String name, @Nonnull String id){
            Objects.requireNonNull(name, id);
            this.name = name;
            this.id = id;
        }

        public Builder usesDepartmentPrefixes(boolean usesDepartmentPrefixes){
            this.usesDepartmentPrefixes = usesDepartmentPrefixes;
            return this;
        }

        public Builder withSection(@Nonnull String name, @Nonnull String code) {
            Objects.requireNonNull(name, code);
            if (!sectionNames.add(name))
                throw new IllegalStateException("Section code \"" + code + "\" defined multiple times");
            if (!sectionCodes.add(code))
                throw new IllegalStateException("Section name \"" + name + "\" defined multiple times");
            this.sections.put(name, code);
            return this;
        }

        public School build() {
            return new School(this);
        }
    }

    private School(Builder b) {

        this.schoolName = b.name;
        this.schoolId = b.id;
        this.useDepartmentPrefixes = b.usesDepartmentPrefixes;

        for (Map.Entry<String, String> e : b.sections.entrySet()){
            this.sectionTypeCodeByName.put(e.getKey(), e.getValue());
            this.sectionTypeNameByCode.put(e.getValue(), e.getKey());
        }
    }

    public static Builder builder(@Nonnull String name, @Nonnull String id){
        return new Builder(name, id);
    }

    public String getSchoolName() {
        return this.schoolName;
    }

    public String getSchoolId() {
        return this.schoolId;
    }

    public boolean usesDepartmentPrefixes() {
        return this.useDepartmentPrefixes;
    }

    public Set<String> getSectionTypeCodes() {
        return this.sectionTypeNameByCode.keySet();
    }

    public String getSectionTypeNameByCode(String code) {

        if (this.sectionTypeNameByCode.containsKey(code))
            return this.sectionTypeNameByCode.get(code);
        else
            throw new IllegalArgumentException("No section name associated with the code \"" + code + "\"");
    }

    public String getSectionTypeCodeByName(String name) {

        if (this.sectionTypeCodeByName.containsKey(name))
            return this.sectionTypeCodeByName.get(name);
        else
            throw new IllegalArgumentException("No section code associated with the name \"" + name + "\"");
    }
}