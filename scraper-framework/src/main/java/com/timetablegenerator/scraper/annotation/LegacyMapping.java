package com.timetablegenerator.scraper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface LegacyMapping {

    enum LegacyType {
        TUTORIAL("tu"), LAB("l"), CORE("c"), UNUSED(null);

        public final String jsonKey;

        LegacyType(String jsonKey){
            this.jsonKey = jsonKey;
        }
    }

    String from();
    LegacyType to();
}