package com.timetablegenerator.model.period;

import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.Term;
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

    private RepeatingPeriod(Term term) {
        super(term);
    }

    public static RepeatingPeriod of(Term term) {
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

    public Optional<DayOfWeek> getDayOfWeek() {
        return this.dayOfWeek == null ? Optional.empty() : Optional.of(this.dayOfWeek);
    }

    public Optional<LocalTime> getStartTime() {
        return this.startTime == null ? Optional.empty() : Optional.of(this.startTime);
    }

    public Optional<LocalTime> getEndTime() {
        return this.endTime == null ? Optional.empty() : Optional.of(this.endTime);
    }

    @Override
    public boolean isScheduled() {
        return this.dayOfWeek != null;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (this.dayOfWeek == null) {
            sb.append("TBA");
        } else {
            sb.append(this.dayOfWeek.toString());
        }

        sb.append(' ');

        if (this.startTime != null && this.endTime != null) {
            sb.append(this.startTime.format(TIME_FORMAT))
                    .append(" -> ")
                    .append(this.endTime.format(TIME_FORMAT));
        } else {
            sb.append("TBA -> TBA");
        }

        sb.append(" [Term: ").append(this.getTerm()).append(']');

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
            sb.append("\n\n").append(I).append("Notes:\n");
            notes.forEach(x -> sb.append('\n')
                    .append(StringUtilities.indent(2, x)));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull RepeatingPeriod that) {

        if (!this.getTerm().equals(that.getTerm())) {
            return this.getTerm().compareTo(that.getTerm());
        }
        if (this.dayOfWeek == null && that.dayOfWeek != null) {
            return -1;
        } else if (this.dayOfWeek != null && that.dayOfWeek == null) {
            return 1;
        } else if (this.dayOfWeek == null) {
            return this.equals(that) ? 0 : -1;
        } else if (this.dayOfWeek != that.dayOfWeek) {
            return this.dayOfWeek.compareTo(that.dayOfWeek);
        } else if (!this.startTime.equals(that.startTime)) {
            return this.startTime.compareTo(that.startTime);
        } else if (!this.endTime.equals(that.endTime)) {
            return this.endTime.compareTo(that.endTime);
        }
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public RepeatingPeriod addSupervisors(String... supervisors) {
        super.addSupervisors(supervisors);
        return this;
    }

    @Override
    public RepeatingPeriod addSupervisors(Collection<String> supervisors) {
        super.addSupervisors(supervisors);
        return this;
    }

    @Override
    public RepeatingPeriod addNotes(String... note) {
        super.addNotes(note);
        return this;
    }

    @Override
    public RepeatingPeriod addNotes(Collection<String> notes) {
        super.addNotes(notes);
        return this;
    }

    @Override
    public RepeatingPeriod setRoom(String room) {
        super.setRoom(room);
        return this;
    }

    @Override
    public RepeatingPeriod setCampus(String campus) {
        super.setCampus(campus);
        return this;
    }

    @Override
    public String getDeltaId() {
        String id = this.getTerm() + "/TBA/TBA/TBA";
        if (this.dayOfWeek != null) {
            id = this.getTerm() + "/"
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