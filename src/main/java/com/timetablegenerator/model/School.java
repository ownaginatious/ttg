package com.timetablegenerator.model;

import com.timetablegenerator.Settings;
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

    private String I = Settings.getIndent();

    public static class Builder {

        private final String name;
        private final String id;
        private final Map<String, String> sections = new HashMap<>();
        private final TermDefinition.Builder superTerm;

        private Builder(@NonNull String id, @NonNull String name){
            Objects.requireNonNull(id, name);
            this.id = id;
            this.name = name;
            this.superTerm = TermDefinition.builder(id + "_terms", id + "_terms", -1);
        }

        public Builder withSection(@NonNull String code, @NonNull String name) {
            if (sections.putIfAbsent(code, name) != null) {
                throw new IllegalArgumentException(
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

    public static Builder builder(@Nonnull String id, @Nonnull String name){
        return new Builder(id, name);
    }

    public Set<String> getSectionTypeCodes() {
        return this.sectionsTypes.keySet();
    }

    public Set<String> getTermCodes() {
        return this.superTerm.getAllSubterms();
    }

    public Term getTermInstance(String code, int year){
        return this.superTerm.getSubterm(code).createForYear(year);
    }

    public String getSectionTypeName(String code) {
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
        // Some standard classes will use this instead of .equals() so ensure non-zero if not equal.
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("* School: ")
                .append(this.name).append(" [").append(this.id).append(']')
                .append('\n').append("* Section types:");
        for (Map.Entry<String, String> e : this.sectionsTypes.entrySet()){
            sb.append('\n').append(I).append("- ").append(e.getKey()).append(" -> ").append(e.getValue());
        }
        sb.append("\n* Terms:");
        for (String termKey : this.superTerm.getSubterms()){
            sb.append('\n').append(I).append("- ").append(this.superTerm.getSubterm(termKey));
        }
        return sb.toString();
    }
}