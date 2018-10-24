package com.timetablegenerator.model.period;

import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.range.DateTimeRange;
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
public class OneTimePeriod extends Period implements Comparable<OneTimePeriod>, Diffable<OneTimePeriod> {

    @Setter @NonNull private DateTimeRange dateTimeRange;

    private OneTimePeriod(Term term) {
        super(term);
    }

    public static OneTimePeriod of(Term term) {
        return new OneTimePeriod(term);
    }

    public Optional<DateTimeRange> getDateTimeRange(){
        return Optional.ofNullable(this.dateTimeRange);
    }

    @Override
    public boolean isScheduled() {
        return this.dateTimeRange != null;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (this.dateTimeRange != null) {
            sb.append(this.dateTimeRange);
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
    public int compareTo(@Nonnull OneTimePeriod that) {
        if (!this.getTerm().temporallyEquals(that.getTerm())) {
            return this.getTerm().compareTo(that.getTerm());
        } else if (!Objects.equals(this.dateTimeRange, that.dateTimeRange)) {
            if (this.dateTimeRange == null) {
                return -1;
            } else if (that.dateTimeRange == null){
                return 1;
            }
            return this.dateTimeRange.compareTo(that.dateTimeRange);
        }
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String getUniqueId() {
        String id = this.getTerm().getUniqueId();
        if (this.dateTimeRange != null) {
            return id + "/" + this.dateTimeRange;
        } else {
            return id + "/TBA";
        }
    }

    @Override
    public String getDeltaId() {
        return this.getUniqueId();
    }

    @Override
    public OneTimePeriod addSupervisors(String... supervisors) {
        super.addSupervisors(supervisors);
        return this;
    }

    @Override
    public OneTimePeriod addSupervisors(Collection<String> supervisors) {
        super.addSupervisors(supervisors);
        return this;
    }

    @Override
    public OneTimePeriod addNotes(String... note) {
        super.addNotes(note);
        return this;
    }

    @Override
    public OneTimePeriod addNotes(Collection<String> notes) {
        super.addNotes(notes);
        return this;
    }

    @Override
    public OneTimePeriod setRoom(String room) {
        super.setRoom(room);
        return this;
    }

    @Override
    public OneTimePeriod setCampus(String campus) {
        super.setCampus(campus);
        return this;
    }

    public StructureDelta findDifferences(OneTimePeriod that) {

        if (!this.getTerm().temporallyEquals(that.getTerm()) || !Objects.equals(this.dateTimeRange, that.dateTimeRange)) {
            throw new IllegalArgumentException(
                    String.format("Cannot compare temporally unequal one-time periods: (%s) and (%s)",
                            this.getDeltaId(), that.getDeltaId()));
        }

        StructureDelta delta = StructureDelta.of(PropertyType.ONE_TIME_PERIOD, this);
        this.savePeriodDifferences(delta, that);

        return delta;
    }
}