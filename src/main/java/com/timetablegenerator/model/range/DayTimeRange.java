package com.timetablegenerator.model.range;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.timetablegenerator.serializer.jackson.DaySerializer;
import com.timetablegenerator.serializer.jackson.LocalTimeSerializer;
import lombok.*;

import javax.annotation.Nonnull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DayTimeRange implements Comparable<DayTimeRange> {

    private static final String START = "startTime";
    private static final String END = "endTime";
    private static final String DOW = "dayOfWeek";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @JsonSerialize(using = DaySerializer.Serializer.class)
    @JsonDeserialize(using = DaySerializer.Deserializer.class)
    @JsonProperty(DOW)
    @Getter
    private final DayOfWeek dayOfWeek;


    @JsonSerialize(using = LocalTimeSerializer.Serializer.class)
    @JsonDeserialize(using = LocalTimeSerializer.Deserializer.class)
    @JsonProperty(START)
    @Getter
    private final LocalTime startTime;

    @JsonSerialize(using = LocalTimeSerializer.Serializer.class)
    @JsonDeserialize(using = LocalTimeSerializer.Deserializer.class)
    @JsonProperty(END)
    @Getter
    private final LocalTime endTime;

    @JsonCreator
    public static DayTimeRange of(@JsonProperty(DOW) @NonNull DayOfWeek dow,
                                  @JsonProperty(START) @NonNull LocalTime startTime,
                                  @JsonProperty(END) @NonNull LocalTime endTime) {
        if (endTime.isBefore(startTime)) { // Catch unconverted AM/PM crossings.
            throw new IllegalArgumentException("The day time range start \"" + startTime
                    + "\" is after the end '" + endTime + "'");
        }
        return new DayTimeRange(dow,
                startTime.truncatedTo(ChronoUnit.MINUTES), endTime.truncatedTo(ChronoUnit.MINUTES));
    }

    @Override
    public int compareTo(@Nonnull DayTimeRange that) {

        if (this.dayOfWeek != that.dayOfWeek) {
            return this.dayOfWeek.compareTo(that.dayOfWeek);
        } else if (!this.startTime.equals(that.startTime)) {
            return this.startTime.compareTo(that.startTime);
        }
        return this.endTime.compareTo(that.endTime);
    }

    @Override
    public String toString() {
        return dayOfWeek.name() + " " +
                this.startTime.format(TIME_FORMAT) + " -> " + this.endTime.format(TIME_FORMAT);
    }
}
