package com.timetablegenerator.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.*;

import static java.lang.String.format;

@EqualsAndHashCode
@Accessors(chain = true)
@ToString()
public class TermDefinition implements Comparable<TermDefinition> {

    @Getter private final String code;
    @Getter private final String name;
    @Getter private final int yearOffset;
    private final int orderingKey;
    private final Map<String, TermDefinition> subterms;

    private TermDefinition(Builder builder) {
        this.code = builder.code;
        this.name = builder.name;
        this.yearOffset = builder.yearOffset;
        this.orderingKey = builder.orderingKey;
        this.subterms = Collections.unmodifiableMap(builder.subterms);
    }

    public static class Builder {

        private final String code;
        private final String name;
        private final int orderingKey;
        private int yearOffset = 0;
        private Map<String, TermDefinition> subterms = new HashMap<>();

        private Builder(String code, String name, int orderingKey){
            this.code = code;
            this.name = name;
            this.orderingKey = orderingKey;
        }

        public Builder withYearOffset(int offset) {
            this.yearOffset = offset;
            return this;
        }

        public Builder withSubterm(@NonNull TermDefinition subterm) {
            if (subterms.putIfAbsent(subterm.code, subterm) != null){
                throw new IllegalArgumentException(
                        format("Multiple definitions for subterm \"%s\" under term \"%s\"", subterm.code, this.code));
            }

            // Ensure it doesn't contain any codes already in use.
            Set<String> existingCodes = new HashSet<>(subterms.keySet());
            existingCodes.add(code);
            Stack<TermDefinition> s = new Stack<>();
            s.add(subterm);
            while (!s.isEmpty()){

            }
            return this;
        }

        public TermDefinition build(){
            return new TermDefinition(this);
        }
    }

    public Builder builder(@NonNull String code, @NonNull String name, int orderingKey){
        return new Builder(code, name, orderingKey);
    }

    public Set<String> getSubterms(){
        return this.subterms.keySet();
    }

    public TermDefinition getSubterm(String code){
        if (!this.subterms.containsKey(code)) {
            throw new IllegalArgumentException(
                    format("No subterm with code \"%s\" in term \"%s\"", code, this.code)
            );
        }
        return this.subterms.get(this.code);
    }

    public Term getInstance(int year){
        return new Term(this, year);
    }

    @Override
    public int compareTo(@Nonnull TermDefinition that) {
        if (this.orderingKey != that.orderingKey) {
            return Integer.valueOf(this.orderingKey).compareTo(that.orderingKey);
        }
        return this.equals(that) ? 0 : -1;
    }
}