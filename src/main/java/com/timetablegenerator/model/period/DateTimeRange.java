package com.timetablegenerator.model.period;

import lombok.*;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeRange implements Comparable<DateTimeRange> {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm");

    @Getter private final LocalDateTime startDateTime;
    @Getter private final LocalDateTime endDateTime;

    public static DateTimeRange of(@NonNull LocalDateTime startDateTime, @NonNull LocalDateTime endDateTime) {
        if (startDateTime.isAfter(endDateTime)){
            if (startDateTime.isAfter(endDateTime)) {
                throw new IllegalStateException("The date dayTimeRange range start date '" + startDateTime
                        + "' is after the end date '" + endDateTime + "'");
            }
        }
        return new DateTimeRange(startDateTime, endDateTime);
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
