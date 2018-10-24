package com.timetablegenerator.scraper;

import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TimeTable;

import java.io.IOException;
import java.util.Set;

public abstract class Scraper {

    private School school;

    public final void init(School school) {
        if (this.school != null)
            return;
        this.school = school;
    }

    protected final School getSchool() {
        return this.school;
    }

    public abstract TimeTable retrieveTimetable(Term term) throws IOException;

    public abstract Set<Term> findAvailableTerms() throws IOException;
}
