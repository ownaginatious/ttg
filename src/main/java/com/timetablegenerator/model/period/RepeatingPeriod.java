package com.timetablegenerator.model.period;

import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.TermClassifier;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public class RepeatingPeriod extends Period implements Comparable<RepeatingPeriod>, Diffable<RepeatingPeriod> {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    private RepeatingPeriod(TermClassifier term) {
        super(term);
    }

    public static RepeatingPeriod of(TermClassifier term) {
        return new RepeatingPeriod(term);
    }

    public RepeatingPeriod setTime(@NonNull DayOfWeek dow, @NonNull LocalTime startTime, @NonNull LocalTime endTime) {

        if (endTime.isBefore(startTime)) { // Catch unconverted AM/PM crossings.
            throw new IllegalStateException("The start time \"" + startTime
                    + "\" is after the end time '" + endTime + "'");
        }

        this.dayOfWeek = dow;
        this.startTime = startTime;
        this.endTime = endTime;

        return this;
    }

    public Optional<DayOfWeek> getDayOfWeek(){
        return this.dayOfWeek == null ? Optional.empty() : Optional.of(this.dayOfWeek);
    }

    public Optional<LocalTime> getStartTime(){
        return this.startTime == null ? Optional.empty() : Optional.of(this.startTime);
    }

    public Optional<LocalTime> getEndTime(){
        return this.endTime == null ? Optional.empty() : Optional.of(this.endTime);
    }

    @Override
    public boolean isScheduled() {
        return this.dayOfWeek != null;
    }

    public RepeatingPeriod addSupervisors(String... supervisors) {

        super.addSupervisors(supervisors);
        return this;
    }

    public RepeatingPeriod addSupervisors(Collection<String> supervisors) {
        super.addSupervisors(supervisors);
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (this.dayOfWeek == null) {
            sb.append("TBA");
        } else {
            sb.append(this.dayOfWeek.toString());
        }

        sb.append(" ");

        if (this.startTime != null && this.endTime != null) {
            sb.append(this.startTime.format(TIME_FORMAT))
                    .append(" -> ")
                    .append(this.endTime.format(TIME_FORMAT));
        } else {
            sb.append("TBA -> TBA");
        }

        sb.append(" [Term: ").append(this.getTerm().toString()).append(']');

        if (this.getCampus().isPresent() || this.getRoom().isPresent()) {

            sb.append(" (");

            this.getCampus().ifPresent(x -> sb.append("campus: ").append(x));
            this.getRoom().ifPresent(x -> sb.append(this.getCampus().isPresent() ? ", " : "")
                                            .append("room: ").append(x));

            sb.append(")");
        }

        this.isOnline().ifPresent(x -> sb.append(x ? " (" : " (not ").append("online)"));

        if (!this.getSupervisors().isEmpty()) {
            sb.append(" [Instructors: ").append(this.getSupervisors().stream()
                    .map(x -> "'" + x + "'")
                    .collect(Collectors.joining(", "))).append(']');
        }

        Collection<String> notes = this.getNotes();

        if (!notes.isEmpty()) {
            sb.append("\n\n").append(TAB).append("Notes:\n");
            notes.forEach(x -> sb.append('\n').append(TAB).append(TAB)
                            .append(StringUtilities.indent(3, "- " + x)));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull RepeatingPeriod rp) {

        int termCompare = this.getTerm().compareTo(rp.getTerm());

        if (termCompare != 0) {
            return termCompare;
        }

        if (this.dayOfWeek == null && rp.dayOfWeek != null) {
            return -1;
        } else if (this.dayOfWeek != null && rp.dayOfWeek == null) {
            return 1;
        } else if (this.dayOfWeek == null){
            return 0;
        } else if (this.dayOfWeek != rp.dayOfWeek) {
            return this.dayOfWeek.compareTo(rp.dayOfWeek);
        }

        return this.startTime.compareTo(rp.startTime);
    }

    @Override
    public String getDeltaId(){
        String id = this.getTerm().getId() + "/TBA/TBA/TBA";
        if (this.dayOfWeek != null) {
            id = this.getTerm().getId() + "/"
                    + this.dayOfWeek.name() + "/"
                    + this.startTime.format(TIME_FORMAT) + "/"
                    + this.endTime.format(TIME_FORMAT);
        }
        return id;
    }

    public StructureChangeDelta findDifferences(RepeatingPeriod that) {

        if (this.getTerm() != that.getTerm() || !Objects.equals(this.startTime, that.startTime)
                || !Objects.equals(this.endTime, that.endTime)) {
            throw new IllegalArgumentException(
                    String.format("Cannot compare temporally unequal repeating periods: (%s) and (%s)",
                            this.getDeltaId(), that.getDeltaId()));
        }

        StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.REPEATING_PERIOD, this);
        this.savePeriodDifferences(delta, that);

        return delta;
    }
}