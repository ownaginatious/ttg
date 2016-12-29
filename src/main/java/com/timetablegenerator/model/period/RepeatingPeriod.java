package com.timetablegenerator.model.period;

import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.range.DateRange;
import com.timetablegenerator.model.range.DayTimeRange;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class RepeatingPeriod extends Period implements Comparable<RepeatingPeriod>, Diffable<RepeatingPeriod> {

    @NonNull @Setter private DayTimeRange dayTimeRange;
    @NonNull @Setter private DateRange activeDateRange;

    private RepeatingPeriod(Term term) {
        super(term);
    }

    public static RepeatingPeriod of(Term term) {
        return new RepeatingPeriod(term);
    }

    public Optional<DayTimeRange> getDayTimeRange(){
        return Optional.ofNullable(this.dayTimeRange);
    }

    public Optional<DateRange> getActiveDateRange(){
        return Optional.ofNullable(this.activeDateRange);
    }

    @Override
    public boolean isScheduled() {
        return this.dayTimeRange != null;
    }

    @Override
    public int compareTo(@Nonnull RepeatingPeriod that) {

        if (!this.getTerm().temporallyEquals(that.getTerm())) {
            return this.getTerm().compareTo(that.getTerm());
        }
        else if (!Objects.equals(this.dayTimeRange, that.dayTimeRange)) {
            if (this.dayTimeRange == null) {
                return -1;
            } else if (that.dayTimeRange == null){
                return 1;
            }
            return this.dayTimeRange.compareTo(that.dayTimeRange);
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
    public RepeatingPeriod setOnline(Boolean online) {
        super.setOnline(online);
        return this;
    }

    @Override
    public String getDeltaId() {
        String id = this.getTerm().getUniqueId();
        if (this.dayTimeRange != null) {
            return id + "/" + this.dayTimeRange;
        } else {
            return id + "/TBA";
        }
    }

    public StructureDelta findDifferences(RepeatingPeriod that) {

        if (!this.getTerm().temporallyEquals(that.getTerm()) || !Objects.equals(this.dayTimeRange, that.dayTimeRange)) {
            throw new IllegalArgumentException(
                    String.format("Cannot compare temporally unequal repeating periods: (%s) and (%s)",
                            this.getDeltaId(), that.getDeltaId()));
        }

        StructureDelta delta = StructureDelta.of(PropertyType.REPEATING_PERIOD, this);
        this.savePeriodDifferences(delta, that);

        return delta;
    }

    public boolean temporallyEquals(RepeatingPeriod that) {
        return this.getTerm().temporallyEquals(that.getTerm()) &&
                this.dayTimeRange.equals(that.dayTimeRange);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (this.activeDateRange != null) {
            sb.append('[').append(this.activeDateRange).append(']');
        }

        if (this.dayTimeRange == null) {
            sb.append("TBA TBA -> TBA");
        } else {
            sb.append(this.dayTimeRange.toString());
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
}