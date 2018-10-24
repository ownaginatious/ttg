package com.timetablegenerator.scraper.school.lakehead;

import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.TimeTable;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.Scraper;

import java.io.IOException;
import java.util.Set;

@SchoolConfig(name = "Lakehead University Orillia Campus", id = "luoc")
public class LakeheadOrilliaScraper extends Scraper {

    private LakeheadScraper scraper = new LakeheadScraper();

    @Override
    public TimeTable retrieveTimetable(Term term) {
        return this.scraper.retrieveTimetable(term);
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {
        return this.scraper.findAvailableTerms(LakeheadScraper.Campus.ORILLIA);
    }
}