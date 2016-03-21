package com.timetablegenerator.scraper.annotation;

import com.timetablegenerator.model.Term;

import javax.annotation.Nonnull;
import java.util.*;

public class LegacySchool {

    private final Term term;
    private final Map<String, LegacyMapping.LegacyType> sections = new HashMap<>();

    public static class Builder {

        private final Term term;
        private final Map<String, LegacyMapping.LegacyType> sections = new HashMap<>();

        public Builder(@Nonnull Term term){
            Objects.requireNonNull(term);
            this.term = term;
        }

        public Builder withMapping(@Nonnull String from, LegacyMapping.LegacyType to) {
            Objects.requireNonNull(from);
            Objects.requireNonNull(to);
            sections.put(from, to);
            return this;
        }

        public LegacySchool build() {
            return new LegacySchool(this);
        }
    }

    private LegacySchool(Builder config) {

        this.term = config.term;
        this.sections.putAll(config.sections);
    }

    public static Builder builder(@Nonnull Term term){
        return new Builder(term);
    }

    public Term getTerm() {
        return this.term;
    }

    public LegacyMapping.LegacyType getLegacyMapping(String key) {
        return this.sections.get(key);
    }
}