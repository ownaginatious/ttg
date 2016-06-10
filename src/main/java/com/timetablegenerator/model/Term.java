package com.timetablegenerator.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Optional;

@EqualsAndHashCode
@Accessors(chain = true)
public class Term implements Comparable<Term> {

    @Getter private final TermDefinition termDefinition;
    @Getter private final int year;
    @Setter private String key;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate examStartDate;
    private LocalDate examEndDate;

    Term(TermDefinition termDefinition, int year) {
        this.termDefinition = termDefinition;
        this.year = year;
    }

    public Term setDates(@NonNull LocalDate start, @NonNull LocalDate end){
        if (start.isAfter(end)){
            throw new IllegalArgumentException(
                    String.format("start (%s) is after end (%s)", start, end)
            );
        }
        this.startDate = start;
        this.endDate = end;
        return this;
    }

    public Term setExamDates(@NonNull LocalDate start, @NonNull LocalDate end){
        if (start.isAfter(end)){
            throw new IllegalArgumentException(
                    String.format("start (%s) is after end (%s)", start, end)
            );
        }
        this.examStartDate = start;
        this.examEndDate = end;
        return this;
    }

    public Optional<LocalDate> getStartDate() {
        return this.startDate != null ? Optional.of(this.startDate) : Optional.empty();
    }

    public Optional<LocalDate> getEndDate() {
        return this.endDate != null ? Optional.of(this.endDate) : Optional.empty();
    }

    public Optional<LocalDate> getExamStartDate() {
        return this.examStartDate != null ? Optional.of(this.examStartDate) : Optional.empty();
    }

    public Optional<LocalDate> getExamEndDate() {
        return this.examEndDate != null ? Optional.of(this.examEndDate) : Optional.empty();
    }

    public Optional<String> getKey() {
        return this.key != null ? Optional.of(this.key) : Optional.empty();
    }

    public Term getSubterm(String code) {
        TermDefinition td = this.termDefinition.getSubterm(code);
        return new Term(td, this.year + td.getYearOffset());
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
        } else if (!this.key.equals(that.key)) {
            return this.key.compareTo(that.key);
        }
        return this.equals(that) ? 0 : -1;
    }
}