package com.timetablegenerator.model.period;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.TermClassifier;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OneTimePeriod extends Period implements Comparable<OneTimePeriod>, Diffable<OneTimePeriod> {

    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm");

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public OneTimePeriod(TermClassifier term) {
        super(term);
    }

    public OneTimePeriod setDateTimes(LocalDateTime startDateTime, LocalDateTime endDateTime) {

        if (startDateTime == null || endDateTime == null)
            throw new IllegalStateException("Attempted to set null start and/or end times.");
        else if (endDateTime.isBefore(startDateTime)) // Catch unaccounted AM/PM crossings.
            throw new IllegalStateException("The start time '" + startDateTime + "' is after the end time '"
                    + endDateTime + "'");

        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;

        return this;
    }

    public LocalDateTime getStartDateTime(){
        return this.startDateTime;
    }

    public LocalDateTime getEndDateTime(){
        return this.endDateTime;
    }

    @Override
    public boolean isScheduled(){
        return this.startDateTime != null;
    }

    @Override
    public boolean equals(Object o) {

        if (!super.equals(o) || !(o instanceof OneTimePeriod))
            return false;

        OneTimePeriod sp = (OneTimePeriod) o;

        return this.endDateTime.equals(sp.endDateTime)
                && this.startDateTime.equals(sp.startDateTime);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        result = 31 * result + startDateTime.hashCode();
        result = 31 * result + endDateTime.hashCode();

        return result;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (this.startDateTime != null && this.endDateTime != null)
            sb.append(this.startDateTime.format(DATETIME_FORMAT))
                    .append(" -> ")
                    .append(this.endDateTime.format(DATETIME_FORMAT));
        else
            sb.append("TBA -> TBA");

        sb.append(" [Term: ").append(term.toString()).append(']');

        if (this.campus != null || this.room != null) {

            sb.append(" (");

            if (this.campus != null)
                sb.append("campus: ").append(this.room);

            if (this.room != null) {
                if (this.campus != null)
                    sb.append(", ");

                sb.append("room: ").append(this.room);
            }

            sb.append(") ");
        }

        if (this.online != null)
            sb.append(" (online: ").append(this.online).append(") ");

        if (supervisors.size() > 0)
            sb.append(", Instructors ").append(this.supervisors);

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull OneTimePeriod sp) {

        if (this.startDateTime != null && sp.startDateTime != null)
            return this.startDateTime.compareTo(sp.startDateTime);
        else if(this.startDateTime == null && sp.startDateTime != null)
            return -1;
        else if (this.startDateTime != null)
            return 1;

        return 0;
    }

    public StructureChangeDelta findDifferences(OneTimePeriod that) {

        if (!(this.startDateTime.equals(that.startDateTime)
                && this.endDateTime.equals(that.endDateTime))) {
            throw new IllegalStateException(
                    String.format("Cannot compare temporaly unequal one-time periods: (%s, %s) and (%s, %s)",
                            this.startDateTime.format(DATETIME_FORMAT), this.endDateTime.format(DATETIME_FORMAT),
                            that.startDateTime.format(DATETIME_FORMAT), that.endDateTime.format(DATETIME_FORMAT))
            );
        }

        String id = this.startDateTime.format(DATETIME_FORMAT) + "/"
                + this.endDateTime.format(DATETIME_FORMAT);

        StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.SINGLE_PERIOD, id);
        this.savePeriodDifferences(delta, that);

        return delta;
    }
}