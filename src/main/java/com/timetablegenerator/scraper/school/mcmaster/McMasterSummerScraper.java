package com.timetablegenerator.scraper.school.mcmaster;

import com.timetablegenerator.model.*;
import com.timetablegenerator.scraper.*;
import static com.timetablegenerator.scraper.annotation.LegacyMapping.LegacyType.*;

import com.timetablegenerator.scraper.annotation.LegacyConfig;
import com.timetablegenerator.scraper.annotation.LegacyMapping;
import com.timetablegenerator.scraper.annotation.SchoolConfig;
import com.timetablegenerator.scraper.annotation.SectionMapping;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SchoolConfig(
        name = "McMaster University", id = "mcmaster_summer",
        useDepartmentPrefixes = true,
        sections = {
                @SectionMapping(name = "Core", code = "C"),
                @SectionMapping(name = "Lab", code = "L"),
                @SectionMapping(name = "Tutorial", code = "T")
        },
        legacy = @LegacyConfig(
                year = 2016, term = TermClassifier.FULL_SUMMER,
                mapping = {
                        @LegacyMapping(from = "C", to = CORE),
                        @LegacyMapping(from = "L", to = LAB),
                        @LegacyMapping(from = "T", to = TUTORIAL)
                }
        )
)
public class McMasterSummerScraper extends Scraper {

    private Scraper getActualScraper() {

        ScraperFactory scraperFactory;

        try {
            scraperFactory =
                    (ScraperFactory) Class.forName(ScraperFactory.class.getCanonicalName() + "Impl").newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);

        }

        return scraperFactory.getScraper("mcmaster").get();
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
