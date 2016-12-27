package com.timetablegenerator.delta;

import com.timetablegenerator.model.*;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.period.RepeatingPeriod;
import lombok.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum PropertyType {

    TERM(TermDefinition.class),
    CAMPUS(String.class), ROOM(String.class), NAME(String.class),
    SERIAL_NUMBER(String.class), CREDITS(Double.class),
    SECTION_TYPE(SectionType.class), SECTION(Section.class),
    REPEATING_PERIOD(RepeatingPeriod.class), ONE_TIME_PERIOD(OneTimePeriod.class),
    TIMETABLE(TimeTable.class), COURSE(Course.class),
    CROSS_LISTING(Course.class), PRE_REQUISITE(Course.class),
    ANTI_REQUISITE(Course.class), CO_REQUISITE(Course.class),
    DESCRIPTION(String.class), NOTE(String.class), WAITING_LIST(Boolean.class),
    NUM_WAITING(Integer.class), MAX_WAITING(Integer.class), IS_FULL(Boolean.class),
    NUM_ENROLLED(Integer.class), MAX_ENROLLED(Integer.class),
    IS_CANCELLED(Boolean.class), IS_ONLINE(Boolean.class),
    SUPERVISOR(String.class);

    private final Class<? extends Comparable> expectedType;

    PropertyType(@NonNull Class<? extends Comparable> expectedType){
        this.expectedType = expectedType;
    }

    public void validateType(Class<?> type){
        if(!this.expectedType.isAssignableFrom(type)){
            throw new IllegalArgumentException("Type " + type.getName() +
                    " is not assignable from expected type " + expectedType.getName() +
                    " for property type [" + this.getFieldName() + "]");
        }
    }

    private static Set<PropertyType> MANY_TYPES = new HashSet<>();

    static {
        MANY_TYPES.addAll(Arrays.asList(
                TIMETABLE, SECTION, COURSE, PRE_REQUISITE, ANTI_REQUISITE,
                CROSS_LISTING, CO_REQUISITE, REPEATING_PERIOD, ONE_TIME_PERIOD,
                SUPERVISOR, NOTE
        ));
    }

    public Class<?> getExpectedType(){
        return this.expectedType;
    }

    public String getFieldName() {
        return this.name();
    }

    public boolean isSingular(){
        return !MANY_TYPES.contains(this);
    }
}