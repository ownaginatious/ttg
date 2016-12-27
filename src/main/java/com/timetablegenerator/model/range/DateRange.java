package com.timetablegenerator.model.range;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.timetablegenerator.serializer.jackson.LocalDateSerializer;
import lombok.*;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateRange implements Comparable<DateRange> {

    private static final String START = "startDate";
    private static final String END = "endDate";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    private final LocalDate startDate;
    private final LocalDate endDate;

    @JsonCreator
    public static DateRange of(@JsonProperty(START) @NonNull LocalDate startDate,
                               @JsonProperty(END) @NonNull LocalDate endDate){
        if (startDate.isAfter(endDate)){
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("The date range start \"" + startDate
                        + "\" is after the end '" + endDate + "'");
            }
        }
        return new DateRange(startDate, endDate);
    }

    @JsonSerialize(using = LocalDateSerializer.Serializer.class)
    @JsonDeserialize(using = LocalDateSerializer.Deserializer.class)
    @JsonProperty(START)
    public LocalDate getStartDate(){
        return this.startDate;
    }

    @JsonSerialize(using = LocalDateSerializer.Serializer.class)
    @JsonDeserialize(using = LocalDateSerializer.Deserializer.class)
    @JsonProperty(END)
    public LocalDate getEndDate(){
        return this.endDate;
    }

    @Override
    public int compareTo(@Nonnull DateRange that) {
        if (!this.startDate.equals(that.startDate)){
            return this.startDate.compareTo(that.startDate);
        }
        return this.endDate.compareTo(that.endDate);
    }

    @Override
    public String toString(){
        return this.startDate.format(DATE_FORMAT) + " -> " + this.endDate.format(DATE_FORMAT);
    }
}
