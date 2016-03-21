package com.timetablegenerator.model;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Term implements Comparable<Term> {

    private static final Pattern digitMatcher = Pattern.compile("^\\d+$");
    private static final int PRESENT_YEAR = LocalDateTime.now().getYear();

    private String key;
    private final TermClassifier termId;
    private final int year;

    /**
     * Creates a {@link Term} object representing a schedulable time of year.
     *
     * @param termId The time of year the term exists in.
     * @param year   The year of the term.
     */
    public Term(TermClassifier termId, int year) {
        this(termId, year, null);
    }

    /**
     * Creates a {@link Term} object representing a schedulable time of year.
     *
     * @param termId The time of year the term exists in.
     * @param year   The year of the term.
     * @param key   A key used by the source for identifying the term.
     *
     */
    public Term(@Nonnull TermClassifier termId, @Nonnull Integer year, String key) {

        this.termId = termId;
        this.year = year;
        this.key = key;
    }


    public String getKey() {
        return this.key;
    }

    public Integer getYear() {
        return this.year;
    }

    public TermClassifier getTermId() {
        return this.termId;
    }

    @Override
    public boolean equals(Object e) {

        if (!(e instanceof Term))
            return false;

        Term term = (Term) e;

        return this.year == term.getYear()
                && this.termId.equals(term.getTermId());
    }

    @Override
    public int hashCode() {

        int result = termId.hashCode();
        result = 31 * result + year;

        return result;
    }

    @Override
    public String toString(){
        return this.termId.toString() + " " + this.year + (this.key != null ? " (" + this.key + ")" : "");
    }

    @Override
    public int compareTo(@Nonnull Term t) {

        if (this.year != t.year)
            return this.year - t.year;

        return this.termId.compareTo(t.termId);
    }

    public static Term findProbableTerm(@Nonnull String termString, String key) {

        boolean isWinter = false, isFall = false, isSpring = false, isSummer = false;
        Set<Integer> yearCandidate = new HashSet<>();

        for (String x : termString.split("\\W")) {

            String word = x.toLowerCase();

            if (!digitMatcher.matcher(word).find()) {

                isWinter = isWinter || word.equals("winter");
                isFall = isFall || word.equals("fall");
                isSpring = isSpring || word.equals("spring");
                isSummer = isSummer || word.equals("summer");

            } else {

                int year = Integer.parseInt(word);

                // Check if realistically a year.
                if (year > PRESENT_YEAR - 10 && year < PRESENT_YEAR + 10)
                    yearCandidate.add(year);
            }
        }

        TermClassifier ti;

        if (!isSummer && isFall && (isWinter || isSpring)) {
            ti = TermClassifier.FULL_SCHOOL_YEAR;
        } else if (!isSummer && !isFall && (isWinter || isSpring)) {
            ti = TermClassifier.SPRING;
        } else if (!isSummer && isFall) {
            ti = TermClassifier.FALL;
        } else if (isSummer && !isFall && !isWinter) {
            ti = TermClassifier.FULL_SUMMER;
        } else
            throw new IllegalArgumentException("Ambiguous term designation. Unable to parse meaningful information "
                    + "from term string \"" + termString + "\"");

        int year;

        if (yearCandidate.size() == 0)
            throw new IllegalArgumentException("No discernible year within header string \"" + termString + "\"");
        else if (ti == TermClassifier.FULL_SCHOOL_YEAR)
            year = Collections.min(yearCandidate);
        else
            year = Collections.max(yearCandidate);

        return new Term(ti, year, key.trim());
    }
}