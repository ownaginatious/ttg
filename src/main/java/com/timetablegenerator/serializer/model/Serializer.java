package com.timetablegenerator.serializer.model;

import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;

import java.util.Map;

public interface Serializer<T> {

    T toInstance(School school, Map<String, Term> terms);

    void fromInstance(T instance);
}
