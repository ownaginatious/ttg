package com.timetablegenerator.model.period;

import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.TermClassifier;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public class OneTimePeriod extends Period implements Comparable<OneTimePeriod>, Diffable<OneTimePeriod> {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm");

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    private OneTimePeriod(TermClassifier term) {
        super(term);
    }

    public static OneTimePeriod of(TermClassifier term) {
        return new OneTimePeriod(term);
    }

    public OneTimePeriod setDateTimes(@NonNull LocalDateTime startDateTime, @NonNull LocalDateTime endDateTime) {

        if (endDateTime.isBefore(startDateTime)) { // Catch unaccounted AM/PM crossings.
            throw new IllegalStateException("The start time '" + startDateTime + "' is after the end time '"
                    + endDateTime + "'");
        }

        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;

        return this;
    }

    public Optional<LocalDateTime> getStartDateTime() {
        return this.startDateTime == null ? Optional.empty() : Optional.of(startDateTime);
    }

    public Optional<LocalDateTime> getEndDateTime() {
        return this.endDateTime == null ? Optional.empty() : Optional.of(endDateTime);
    }

    @Override
    public boolean isScheduled() {
        return this.startDateTime != null;
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
    public int compareTo(@Nonnull OneTimePeriod that) {

        if (this.getTerm() != that.getTerm()) {
            return this.getTerm().compareTo(that.getTerm());
        } else if (this.startDateTime != null && that.startDateTime != null) {
            return this.startDateTime.compareTo(that.startDateTime);
        } else if (this.startDateTime == null && that.startDateTime != null) {
            return -1;
        } else if (this.startDateTime != null) {
            return 1;
        }

        return 0;
    }

    @Override
    public String getDeltaId(){
        String id = this.getTerm().getId() + "/TBA/TBA/TBA";
        if (this.startDateTime != null) {
            id = this.getTerm().getId() + "/" + this.startDateTime.format(DATETIME_FORMAT) + "/"
                    + this.endDateTime.format(DATETIME_FORMAT);
        }
        return id;
    }

    public StructureChangeDelta findDifferences(OneTimePeriod that) {

        if (this.getTerm() != that.getTerm() || !Objects.equals(this.startDateTime, that.startDateTime) ||
                !Objects.equals(this.endDateTime, that.endDateTime)) {
            throw new IllegalArgumentException(
                    String.format("Cannot compare temporally unequal one-time periods: (%s) and (%s)",
                            this.getDeltaId(), that.getDeltaId()));
        }

        StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.ONE_TIME_PERIOD, this);
        this.savePeriodDifferences(delta, that);

        return delta;
    }
}