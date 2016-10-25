package com.timetablegenerator.model.period;

import lombok.*;

import javax.annotation.Nonnull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DayTimeRange implements Comparable<DayTimeRange> {


    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Getter private final DayOfWeek dayOfWeek;
    @Getter private final LocalTime startTime;
    @Getter private final LocalTime endTime;

    public static DayTimeRange of(@NonNull DayOfWeek dow, @NonNull LocalTime startTime, @NonNull LocalTime endTime) {

        if (endTime.isBefore(startTime)) { // Catch unconverted AM/PM crossings.
            throw new IllegalStateException("The start dayTimeRange \"" + startTime
                    + "\" is after the end dayTimeRange '" + endTime + "'");
        }
        return new DayTimeRange(dow, startTime, endTime);
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
