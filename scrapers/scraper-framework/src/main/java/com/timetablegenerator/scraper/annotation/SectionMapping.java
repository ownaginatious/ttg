package com.timetablegenerator.scraper.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface SectionMapping {

    String code();
    String name();
}