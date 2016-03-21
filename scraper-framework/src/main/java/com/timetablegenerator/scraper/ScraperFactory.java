package com.timetablegenerator.scraper;

import com.timetablegenerator.scraper.annotation.LegacySchool;

import java.util.Optional;

public interface ScraperFactory {

    Optional<Scraper> getScraper(String schoolId);
    Optional<LegacySchool> getLegacyConfig(String schoolId);
}
