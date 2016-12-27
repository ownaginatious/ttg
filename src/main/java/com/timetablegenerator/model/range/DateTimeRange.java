package com.timetablegenerator.model.range;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.timetablegenerator.serializer.jackson.LocalDateTimeSerializer;
import lombok.*;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeRange implements Comparable<DateTimeRange> {

    private static final String START = "startDateTime";
    private static final String END = "endDateTime";

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm");

    @JsonSerialize(using = LocalDateTimeSerializer.Serializer.class)
    @JsonDeserialize(using = LocalDateTimeSerializer.Deserializer.class)
    @JsonProperty(START)
    @Getter
    private final LocalDateTime startDateTime;

    @JsonSerialize(using = LocalDateTimeSerializer.Serializer.class)
    @JsonDeserialize(using = LocalDateTimeSerializer.Deserializer.class)
    @JsonProperty(END)
    @Getter
    private final LocalDateTime endDateTime;

    @JsonCreator
    public static DateTimeRange of(@JsonProperty(START) @NonNull LocalDateTime startDateTime,
                                   @JsonProperty(END) @NonNull LocalDateTime endDateTime) {
        if (startDateTime.isAfter(endDateTime)){
            if (startDateTime.isAfter(endDateTime)) {
                throw new IllegalArgumentException("The datetime range start '" + startDateTime
                        + "' is after the end '" + endDateTime + "'");
            }
        }
        return new DateTimeRange(startDateTime.truncatedTo(ChronoUnit.MINUTES),
                endDateTime.truncatedTo(ChronoUnit.MINUTES));
    }

    @Override
    public int compareTo(@Nonnull DateTimeRange that) {
        if (!this.startDateTime.equals(that.startDateTime)) {
            return this.startDateTime.compareTo(that.startDateTime);
        }
        return this.endDateTime.compareTo(that.endDateTime);
    }

    @Override
    public String toString(){
        return this.startDateTime.format(DATETIME_FORMAT) + " -> " + this.endDateTime.format(DATETIME_FORMAT);
    }
}
