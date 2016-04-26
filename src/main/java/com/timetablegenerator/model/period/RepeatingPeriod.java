package com.timetablegenerator.model.period;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.TermClassifier;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;

public class RepeatingPeriod extends Period implements Comparable<RepeatingPeriod>, Diffable<RepeatingPeriod> {

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Getter private DayOfWeek dayOfWeek;
    @Getter private LocalTime startTime;
    @Getter private LocalTime endTime;

    public RepeatingPeriod(TermClassifier term) {
        super(term);
    }

    public RepeatingPeriod setTime(DayOfWeek dow, LocalTime startTime, LocalTime endTime) {

        if (dow == null)
            throw new IllegalStateException("Attempted to set null day of the week into repeating time period.");
        if (startTime == null || endTime == null)
            throw new IllegalStateException("Attempted to set null start and/or end times.");
        else if (endTime.isBefore(startTime)) // Catch unaccounted AM/PM crossings.
                throw new IllegalStateException("The start time \"" + startTime
                        + "\" is after the end time '" + endTime + "'");

        this.dayOfWeek = dow;
        this.startTime = startTime;
        this.endTime = endTime;

        return this;
    }

    @Override
    public boolean isScheduled(){
        return this.dayOfWeek != null;
    }

    public RepeatingPeriod addSupervisors(String... supervisors){

        Collections.addAll(this.supervisors, supervisors);
        return this;
    }

    public RepeatingPeriod addSupervisors(Collection<String> supervisors){
        this.supervisors.addAll(supervisors);
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (this.dayOfWeek == null)
            sb.append("TBA");
        else
            sb.append(this.dayOfWeek.toString());

        sb.append(" ");

        if (this.startTime != null && this.endTime != null)
            sb.append(this.startTime.format(TIME_FORMAT))
                    .append(" -> ")
                    .append(this.endTime.format(TIME_FORMAT));
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

            sb.append(")");
        }

        if (this.online != null)
            sb.append(" (online: ").append(this.online).append(')');

        if (supervisors.size() > 0)
            sb.append(" [Instructors: ").append(this.supervisors).append(']');

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (o == null || !(o instanceof RepeatingPeriod))
            return false;

        RepeatingPeriod rp = (RepeatingPeriod) o;

        return this.dayOfWeek == rp.dayOfWeek
                && this.endTime.equals(rp.endTime)
                && this.startTime.equals(rp.startTime)
                && this.term.equals(rp.term);
    }

    @Override
    public int hashCode() {

        int result = super.hashCode();

        result = 31 * result + dayOfWeek.hashCode();
        result = 31 * result + startTime.hashCode();
        result = 31 * result + endTime.hashCode();

        return result;
    }

    @Override
    public int compareTo(@Nonnull RepeatingPeriod rp) {

        int termCompare = this.term.compareTo(rp.term);

        if (termCompare != 0)
            return  termCompare;

        int dayOfWeekComparison = 0;

        if (this.dayOfWeek != null && rp.dayOfWeek != null)
            dayOfWeekComparison = this.dayOfWeek.compareTo(rp.dayOfWeek);
        else if(this.dayOfWeek == null && rp.dayOfWeek != null)
            dayOfWeekComparison = -1;
        else if(this.dayOfWeek != null)
            dayOfWeekComparison = 1;

        if (dayOfWeekComparison != 0)
            return dayOfWeekComparison;

        if (this.startTime != null && rp.startTime != null)
            return this.startTime.compareTo(rp.startTime);
        else if(this.startTime == null && rp.startTime != null)
            return -1;
        else if (this.startTime != null)
            return 1;

        return 0;
    }

    public StructureChangeDelta findDifferences(RepeatingPeriod that) {

        if (!(this.startTime.equals(that.startTime)
                && this.endTime.equals(that.endTime))) {
            throw new IllegalStateException(
                    String.format("Cannot compare temporally unequal repeating periods: (%s, %s, %s) and (%s, %s, %s)",
                            this.dayOfWeek.toString(), this.startTime.format(TIME_FORMAT), this.endTime.format(TIME_FORMAT),
                            that.dayOfWeek.toString(), that.startTime.format(TIME_FORMAT), that.endTime.format(TIME_FORMAT))
            );
        }

        String id = this.dayOfWeek.getValue() + "/" + this.startTime.format(TIME_FORMAT) + "/"
                + this.endTime.format(TIME_FORMAT);

        StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.REPEATING_PERIOD, id);
        this.savePeriodDifferences(delta, that);

        return delta;
    }
}