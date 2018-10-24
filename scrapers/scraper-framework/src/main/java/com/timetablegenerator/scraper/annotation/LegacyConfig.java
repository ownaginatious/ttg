package com.timetablegenerator.scraper.annotation;

import com.timetablegenerator.model.TermClassifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface LegacyConfig {

    TermClassifier term();
    int year();
    LegacyMapping[] mapping() default {};
}