package com.timetablegenerator.serializer.model;

import com.timetablegenerator.model.Department;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class SerializerContext {

    @Getter private final School school;
    private final Map<String, Term> termMap = new HashMap<>();
    private final Map<String, Department> departmentMap = new HashMap<>();

    public static SerializerContext of(School school, Term[] terms, Department[] departments){
        return new SerializerContext(school, terms, departments);
    }

    private SerializerContext(@NonNull School school, @NonNull Term[] terms,
                              @NonNull Department[] departments) {
        this.school = school;
        Arrays.stream(terms).forEach(t -> termMap.put(t.getUniqueId(), t));
        Arrays.stream(departments).forEach(d -> departmentMap.put(d.getCode(), d));
    }

    public Term getTerm(String termId) {
        return this.termMap.get(termId);
    }

    public Department getDepartment(String departmentCode) {
        return this.departmentMap.get(departmentCode);
    }
}
