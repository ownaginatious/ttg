package com.timetablegenerator.model;

import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

import static java.lang.String.format;

public class School implements Comparable<School>{

    @Getter private final String name;
    @Getter private final String id;

    private final Map<String, String> sectionsTypes;
    private final TermDefinition superTerm;

    public static class Builder {

        private final String name;
        private final String id;
        private final Map<String, String> sections = new HashMap<>();
        private final TermDefinition.Builder superTerm;

        private Builder(@NonNull String name, @NonNull String id){
            Objects.requireNonNull(name, id);
            this.name = name;
            this.id = id;
            this.superTerm = TermDefinition.builder(id + "_terms", id + "_terms", -1);
        }

        public Builder withSection(@NonNull String code, @NonNull String name) {
            if (sections.putIfAbsent(code, name) != null) {
                throw new IllegalStateException(
                        format("Multiple definitions for section with code \"%s\" under school \"%s\"", code, id)
                );
            }
            return this;
        }

        public Builder withTerm(@NonNull TermDefinition td) {
            superTerm.withSubterm(td);
            return this;
        }

        public School build() {
            return new School(this);
        }
    }

    private School(Builder builder) {
        this.name = builder.name;
        this.id = builder.id;
        this.sectionsTypes = Collections.unmodifiableMap(builder.sections);
        this.superTerm = builder.superTerm.build();
    }

    public static Builder builder(@Nonnull String name, @Nonnull String id){
        return new Builder(name, id);
    }

    public Set<String> getSectionTypeCodes() {
        return this.sectionsTypes.keySet();
    }

    public Set<String> getTermCodes() {
        return this.superTerm.getSubterms();
    }

    public Term getTermInstance(String code, int year){
        return this.superTerm.getSubterm(code).createForYear(year);
    }

    public String getSectionTypeNameByCode(String code) {
        if (!this.sectionsTypes.containsKey(code)) {
            throw new IllegalArgumentException("No section name associated with the code \"" + code + "\"");
        } else {
            return this.sectionsTypes.get(code);
        }
    }

    @Override
    public int compareTo(@Nonnull School that) {
        if (!this.id.equals(that.id)){
            return this.id.compareTo(that.id);
        } else if (!this.name.equals(that.name)){
            return this.name.compareTo(that.name);
        }
        return this.equals(that) ? 0 : -1;
    }
}