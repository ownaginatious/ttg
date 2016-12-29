package com.timetablegenerator.serializer.model;

import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class SerializerContext {

    @Getter private final School school;
    private final Map<String, Term> termMap = new HashMap<>();

    public static SerializerContext of(School school, Term[] terms){
        return new SerializerContext(school, terms);
    }

    private SerializerContext(School school, Term[] terms) {
        this.school = school;
        Arrays.stream(terms).forEach(t -> termMap.put(t.getUniqueId(), t));
    }

    public Term getTerm(String termId) {
        return termMap.get(termId);
    }
}
