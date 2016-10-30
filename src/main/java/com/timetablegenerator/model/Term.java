package com.timetablegenerator.model;

import com.timetablegenerator.model.range.DateRange;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.*;

@EqualsAndHashCode
@Accessors(chain = true)
public class Term implements Comparable<Term> {

    @Getter private final TermDefinition termDefinition;
    @Getter private final int year;
    @Setter private String key;
    @Setter @NonNull private DateRange dateRange;
    @Setter @NonNull private DateRange examDateRange;

    private Map<String, Term> prebuiltTerms = new HashMap<>();

    Term(TermDefinition termDefinition, int year) {
        this.termDefinition = termDefinition;
        this.year = year;
        for (String termCode : this.termDefinition.getSubterms()) {
            TermDefinition subtermDefinition = this.termDefinition.getSubterm(termCode);
            Term subterm = subtermDefinition.createForYear(year + subtermDefinition.getYearOffset());
            this.prebuiltTerms.putAll(subterm.prebuiltTerms);
            this.prebuiltTerms.put(subtermDefinition.getCode(), subterm);
        }
    }

    public Optional<DateRange> getDateRange() {
        return Optional.ofNullable(this.dateRange);
    }

    public Optional<DateRange> getExamDateRange() {
        return Optional.ofNullable(this.examDateRange);
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(this.key);
    }

    public Term getSubterm(String code) {
        // Try to get the term from the definition to verify it exists.
        this.termDefinition.getSubterm(code);
        return this.prebuiltTerms.get(code);
    }

    @Override
    public String toString() {
        if (this.key != null) {
            return String.format("%s %d (key: %s)", this.termDefinition.toString(), this.year, this.key);
        } else {
            return String.format("%s %d", this.termDefinition.toString(), this.year);
        }
    }

    @Override
    public int compareTo(@Nonnull Term that) {
        if (!this.termDefinition.equals(that.termDefinition)) {
            return this.termDefinition.compareTo(that.termDefinition);
        } else if (this.year != that.year) {
            return Integer.valueOf(this.year).compareTo(that.year);
        } else if (!Objects.equals(this.key, that.key)) {
            if (this.key == null){
                return -1;
            } else if (that.key == null){
                return 1;
            }
            return this.key.compareTo(that.key);
        }
        // Some standard classes will use this instead of .equals() so ensure non-zero if not equal.
        return this.equals(that) ? 0 : -1;
    }
}