package com.timetablegenerator.model;

import java.util.HashMap;
import java.util.Map;

public enum TermClassifier {

    FALL_FIRST_QUARTER(0),
    FALL_SECOND_QUARTER(1),
    FALL(2),
    SPRING_FIRST_QUARTER(3),
    SPRING_SECOND_QUARTER(4),
    SPRING(5),
    FULL_SCHOOL_YEAR(6), // Covering from fall to spring.
    SUMMER_ONE_FIRST_QUARTER(7),
    SUMMER_ONE_SECOND_QUARTER(8),
    SUMMER_ONE(9),
    SUMMER_TWO_FIRST_QUARTER(10),
    SUMMER_TWO_SECOND_QUARTER(11),
    SUMMER_TWO(12),
    FULL_SUMMER(13),
    FULL_YEAR(14), // Covering spring through summer and fall.
    NOT_OFFERED(15),
    UNSCHEDULED(16);

    private static Map<Integer, TermClassifier> idToTermClassifier = new HashMap<>();

    static {
        for (TermClassifier tc : TermClassifier.values())
            idToTermClassifier.put(tc.getId(), tc);
    }

    public static TermClassifier getTermClassifier(int id){
        return idToTermClassifier.get(id);
    }

    private final int id;

    TermClassifier(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}