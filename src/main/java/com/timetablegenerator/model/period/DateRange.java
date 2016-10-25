package com.timetablegenerator.model.period;

import lombok.*;

import javax.annotation.Nonnull;
import java.time.LocalDate;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateRange implements Comparable<DateRange> {

    @Getter private final LocalDate startDate;
    @Getter private final LocalDate endDate;

    public static DateRange of(@NonNull LocalDate startDate, @NonNull LocalDate endDate){
        if (startDate.isAfter(endDate)){
            if (startDate.isAfter(endDate)) {
                throw new IllegalStateException("The date range start date \"" + startDate
                        + "\" is after the end date '" + endDate + "'");
            }
        }
        return new DateRange(startDate, endDate);
    }

    @Override
    public int compareTo(@Nonnull DateRange that) {
        if (!this.startDate.equals(that.startDate)){
            return this.startDate.compareTo(that.startDate);
        }
        return this.endDate.compareTo(that.endDate);
    }
}
