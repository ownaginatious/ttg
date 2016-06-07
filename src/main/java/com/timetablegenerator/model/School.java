package com.timetablegenerator.model;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

public class School implements Comparable<School>{

    @Getter private final String schoolName;
    @Getter private final String schoolId;

    private final Map<String, String> sectionsTypes;
    private final Map<String, TermDefinition> terms;

    public static class Builder {

        private final String name;
        private final String id;
        private final Map<String, String> sections = new HashMap<>();
        private final Map<String, TermDefinition> terms = new HashMap<>();

        private Builder(@NonNull String name, @NonNull String id){
            Objects.requireNonNull(name, id);
            this.name = name;
            this.id = id;
        }

        public Builder withSection(@NonNull String code, @NonNull String name) {
            if (sections.putIfAbsent(code, name) != null) {
                throw new IllegalStateException("Multiple definitions for section with code \"" + code + "\"");
            }
            return this;
        }

        public Builder withTerm(@NonNull TermDefinition td) {
            if (terms.putIfAbsent(td.getCode(), td) != null) {
                throw new IllegalStateException("Multiple definitions for term with code \"" + td.getCode() + "\"");
            }
            return this;
        }

        public School build() {
            return new School(this);
        }
    }

    private School(Builder b) {

        this.schoolName = b.name;
        this.schoolId = b.id;
        this.sectionsTypes = Collections.unmodifiableMap(b.sections);
        this.terms = Collections.unmodifiableMap(b.terms);
    }

    public static Builder builder(@Nonnull String name, @Nonnull String id){
        return new Builder(name, id);
    }

    public Set<String> getSectionTypes() {
        return this.sectionsTypes.keySet();
    }

    public Set<String> getTermCodes() {
        return this.terms.keySet();
    }

    public String getSectionTypeNameByCode(String code) {
        if (this.sectionsTypes.containsKey(code)) {
            throw new IllegalArgumentException("No section name associated with the code \"" + code + "\"");
        }
        return this.sectionsTypes.get(code);
    }

    @Override
    public int compareTo(@Nonnull School that) {
        if (!this.schoolId.equals(that.schoolId)){
            return this.schoolId.compareTo(that.schoolId);
        } else if (!this.schoolName.equals(that.schoolName)){
            return this.schoolName.compareTo(that.schoolName);
        }
        return this.equals(that) ? 0 : -1;
    }
}