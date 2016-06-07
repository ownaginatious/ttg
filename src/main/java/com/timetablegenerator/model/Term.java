package com.timetablegenerator.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.util.Optional;

@EqualsAndHashCode
@Accessors(chain = true)
@ToString
public class Term implements Comparable<Term> {

    @Getter private final TermDefinition termDefinition;
    @Getter private final int year;
    @Setter private String key;
    @Setter private LocalDate startDate;
    @Setter private LocalDate endDate;

    Term(TermDefinition termDefinition, int year){
        this.termDefinition = termDefinition;
        this.year = year;
    }

    public Optional<LocalDate> getStartDate(){
        return this.startDate != null ? Optional.of(this.startDate) : Optional.empty();
    }

    public Optional<LocalDate> getEndDate(){
        return this.endDate != null ? Optional.of(this.endDate) : Optional.empty();
    }

    public Optional<String> getKey(){
        return this.key != null ? Optional.of(this.key) : Optional.empty();
    }

    public Term getSubterm(String code){
        TermDefinition td = this.termDefinition.getSubterm(code);
        return new Term(td, this.year + td.getYearOffset());
    }

    @Override
    public int compareTo(@Nonnull Term term) {
        return 0;
    }
}