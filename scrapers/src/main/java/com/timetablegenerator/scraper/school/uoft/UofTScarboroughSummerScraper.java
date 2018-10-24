package com.timetablegenerator.scraper.school.uoft;

import com.timetablegenerator.model.*;
import com.timetablegenerator.scraper.*;
import com.timetablegenerator.scraper.annotation.LegacyConfig;
import com.timetablegenerator.scraper.annotation.LegacyMapping;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.annotation.SectionMapping;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.timetablegenerator.scraper.annotation.LegacyMapping.LegacyType.*;

@SchoolConfig(
        name = "University of Toronto Scarborough", id = "utsc_summer",
        sections = {
                @SectionMapping(name = "Lecture", code = "LEC"),
                @SectionMapping(name = "Practical", code = "PRA"),
                @SectionMapping(name = "Tutorial", code = "TUT")
        },
        legacy = @LegacyConfig(
                year = 2018, term = TermClassifier.FULL_SUMMER,
                mapping = {
                        @LegacyMapping(from = "LEC", to = CORE),
                        @LegacyMapping(from = "PRA", to = LAB),
                        @LegacyMapping(from = "TUT", to = TUTORIAL)
                }
        )
)
public class UofTScarboroughSummerScraper extends Scraper {

    private Scraper getActualScraper() {

        ScraperFactory scraperFactory;

        try {
            scraperFactory =
                    (ScraperFactory) Class.forName(ScraperFactory.class.getCanonicalName() + "Impl").newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

        return scraperFactory.getScraper("utsc").get();
    }

    @Override
    public TimeTable retrieveTimetable(Term term) throws IOException {
        return this.getActualScraper().retrieveTimetable(term);
    }

    @Override
    public Set<Term> findAvailableTerms() throws IOException {
        return this.getActualScraper().findAvailableTerms().stream()
                .filter(t -> t.getTermId() == TermClassifier.FULL_SUMMER)
                .collect(Collectors.toSet());
    }
}
