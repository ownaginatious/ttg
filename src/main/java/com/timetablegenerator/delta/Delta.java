package com.timetablegenerator.delta;

import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class used to record the property changes of major data type within a time table
 * (e.g. timetable, period. etc).
 *
 */
public abstract class Delta {

    protected static final String TAB = "    ";

    @Getter protected final PropertyType propertyType;

    public Delta(@Nonnull PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    protected static String generateTabs(int tabAmount) {
        return  Stream.generate(() -> TAB).limit(tabAmount).collect(Collectors.joining());
    }

    protected static String fixPrinting(String tabbing, Object value) {

        StringBuilder sb = new StringBuilder();
        String valueString = value.toString();

        if (valueString.contains("\n")) {

            sb.append('\n').append('"');

            for (String line : valueString.split("\n"))
                sb.append('\n').append(tabbing).append(line);

            sb.append('"');

        } else {
            sb.append('"').append(value).append('"');
        }

        return sb.toString();
    }
}