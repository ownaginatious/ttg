package com.timetablegenerator.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.model.period.OneTimePeriod;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.*;

@EqualsAndHashCode()
@Accessors(chain = true)
public class Section implements Diffable<Section> {

    private static final String I = Settings.getIndent();

    @Setter private String serialNumber;
    @Getter private final String sectionId;

    private Boolean waitingList;

    private int waiting = -1;
    private int maxWaiting = -1;

    private Boolean full;

    private int enrollment = -1;
    private int maxEnrollment = -1;


    private final Set<RepeatingPeriod> repeatingPeriods = new TreeSet<>();
    private final Set<OneTimePeriod> oneTimePeriods = new TreeSet<>();

    private final List<String> notes = new ArrayList<>();

    @Setter private Boolean cancelled = null;
    @Setter private Boolean online = null;
    @Setter private Boolean alternating = null;

    private Section(@NonNull String sectionId) {
        this.sectionId = sectionId;
    }

    public static Section of(String sectionId) {
        return new Section(sectionId);
    }

    public Optional<String> getSerialNumber() {
        return this.serialNumber == null ? Optional.empty() : Optional.of(this.serialNumber);
    }

    public Section addNotes(@NonNull Collection<String> notes) {
        this.notes.addAll(notes);
        return this;
    }

    public Section addNotes(@NonNull String... notes) {
        Collections.addAll(this.notes, notes);
        return this;
    }

    public List<String> getNotes() {
        return new ArrayList<>(this.notes);
    }

    public Optional<Boolean> isOnline() {
        return this.online == null ? Optional.empty() : Optional.of(this.online);
    }

    public Optional<Boolean> isCancelled() {
        return this.cancelled == null ? Optional.empty() : Optional.of(this.cancelled);
    }

    public Optional<Boolean> isAlternating() {
        return this.alternating == null ? Optional.empty() : Optional.of(this.alternating);
    }

    public Section setWaitingList(boolean waitingList) {

        this.waitingList = waitingList;

        if (!this.waitingList) {
            this.waiting = -1;
        }
        return this;
    }

    public Optional<Boolean> hasWaitingList() {
        return this.waitingList == null ? Optional.empty() : Optional.of(this.waitingList);
    }

    public Optional<Integer> getWaiting() {
        return this.waiting == -1 ? Optional.empty() : Optional.of(this.waiting);
    }

    public Section setWaiting(int waiting) {
        if (waiting < 0) {
            throw new IllegalArgumentException("Waiting number must be greater than or equal to 0 (" + waiting + ")");
        } else if (this.maxWaiting >= 0 && waiting > this.maxWaiting) {
            throw new IllegalArgumentException("Waiting number must be less than the maximum ("
                    + waiting + "/" + this.maxWaiting + ")");
        }
        this.waiting = waiting;
        this.waitingList = true;
        return this;
    }

    public Optional<Integer> getMaxWaiting() {
        return this.maxWaiting == -1 ? Optional.empty() : Optional.of(this.maxWaiting);
    }

    public Section setMaximumWaiting(int maxWaiting) {
        if (maxWaiting < 0) {
            throw new IllegalArgumentException("Maximum number of people waiting must be greater than or equal to 0 ("
                    + maxWaiting + ")");
        } else if (this.waiting >= 0 && this.waiting > maxWaiting) {
            throw new IllegalArgumentException("Number of people waiting must be less than or equal to the maximum ("
                    + this.waiting + "/" + maxWaiting + ")");
        }
        this.maxWaiting = maxWaiting;
        return this;
    }

    public Section setFull(boolean full) {

        this.full = full;

        if (!this.full) {
            this.enrollment = -1;
        } else {
            if (this.maxEnrollment != -1)
                this.enrollment = this.maxEnrollment;
            if (this.maxEnrollment == -1 && this.enrollment != -1)
                this.maxEnrollment = this.enrollment;
        }

        return this;
    }

    public Optional<Boolean> isFull() {
        return this.full == null ? Optional.empty() : Optional.of(this.full);
    }

    public Optional<Integer> getEnrollment() {
        return this.enrollment == -1 ? Optional.empty() : Optional.of(enrollment);
    }

    public Section setEnrollment(int enrollment) {

        if (enrollment < 0) {
            throw new IllegalArgumentException("Enrollment must be greater than or equal to 0 (" + enrollment + ")");
        } else if (this.maxEnrollment >= 0 && enrollment > this.maxEnrollment) {
            throw new IllegalArgumentException("Number of people enrolled must be less than the maximum ("
                    + enrollment + "/" + this.maxEnrollment + ")");
        }
        this.enrollment = enrollment;

        if (this.maxEnrollment >= 0) {
            this.full = this.enrollment == this.maxEnrollment;
        }
        return this;
    }

    public Optional<Integer> getMaxEnrollment() {
        return this.maxEnrollment == -1 ? Optional.empty() : Optional.of(this.maxEnrollment);
    }

    public Section setMaximumEnrollment(int maxEnrollment) {

        if (maxEnrollment < 0) {
            throw new IllegalArgumentException("Maximum number of people enrolled must be greater than or equal to 0 ("
                    + maxWaiting + ")");
        } else if (this.enrollment >= 0 && this.enrollment > maxEnrollment) {
            throw new IllegalArgumentException("Number of people enrolled must be less than the maximum ("
                    + this.enrollment + "/" + maxEnrollment + ")");
        }

        this.maxEnrollment = maxEnrollment;

        if (this.enrollment >= 0) {
            this.full = this.enrollment == this.maxEnrollment;
        }

        return this;
    }

    public Section addPeriod(OneTimePeriod period){
        this.oneTimePeriods.add(period);
        return this;
    }

    public Section addPeriod(RepeatingPeriod period){
        this.repeatingPeriods.add(period);
        return this;
    }

    public Collection<RepeatingPeriod> getRepeatingPeriods() {
        return new TreeSet<>(this.repeatingPeriods);
    }

    public Collection<OneTimePeriod> getOneTimePeriods() {
        return new TreeSet<>(this.oneTimePeriods);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(this.sectionId);

        this.getSerialNumber().ifPresent(x -> sb.append(" {").append(this.serialNumber).append('}'));
        this.isCancelled().ifPresent(x -> sb.append(x ? " [CANCELLED]" : ""));
        this.isOnline().ifPresent(x -> sb.append(x ? " [ONLINE]" : ""));
        this.isAlternating().ifPresent(x -> sb.append(x ? " [ALTERNATES]" : ""));
        this.isFull().ifPresent(x -> sb.append(x ? " [FULL]" : " [AVAILABLE]"));

        if (this.getEnrollment().isPresent() || this.getMaxEnrollment().isPresent()) {
            sb.append(" [enrolled: ")
                    .append(this.getEnrollment().map(Object::toString).orElse("?"))
                    .append('/')
                    .append(this.getMaxEnrollment().map(Object::toString).orElse("?"))
                    .append(']');
        }

        if (this.getWaiting().isPresent()) {
            sb.append(" [waiting: ")
                    .append(this.getWaiting().map(Object::toString).orElse("?"))
                    .append('/')
                    .append(this.getMaxWaiting().map(Object::toString).orElse("?"))
                    .append(']');
        }

        if (!this.notes.isEmpty()) {
            sb.append("\n\n").append(I).append("Notes:\n");
            this.notes.forEach(x ->
                sb.append('\n').append(StringUtilities.indent(2, x)));
        }

        if (!this.repeatingPeriods.isEmpty()) {
            sb.append("\n\n").append(I).append("Repeating periods:");
            this.repeatingPeriods.forEach(x -> sb.append("\n\n")
                    .append(StringUtilities.indent(2, x.toString())));
        }

        if (!this.oneTimePeriods.isEmpty()) {
            sb.append("\n\n").append(I).append("One time periods:");
            this.oneTimePeriods.forEach(x -> sb.append("\n\n")
                    .append(StringUtilities.indent(2, x.toString())));
        }

        return sb.toString();
    }

    @Override
    public String getDeltaId(){
        return this.getSectionId();
    }

    @Override
    public StructureDelta findDifferences(Section that) {

        if (!this.sectionId.equals(that.sectionId)) {
            throw new IllegalArgumentException("Sections are not related: \"" + this.sectionId
                    + "\" and \"" + that.sectionId + "\"");
        }

        final StructureDelta delta = StructureDelta.of(PropertyType.SECTION, this);

        delta.addValueIfChanged(PropertyType.SERIAL_NUMBER, this.serialNumber, that.serialNumber);

        delta.addValueIfChanged(PropertyType.WAITING_LIST, this.waitingList, that.waitingList);
        delta.addValueIfChanged(PropertyType.NUM_WAITING,
                this.getWaiting().orElse(null), that.getWaiting().orElse(null));
        delta.addValueIfChanged(PropertyType.MAX_WAITING,
                this.getMaxWaiting().orElse(null), that.getMaxWaiting().orElse(null));

        delta.addValueIfChanged(PropertyType.IS_FULL, this.full, that.full);
        delta.addValueIfChanged(PropertyType.NUM_ENROLLED,
                this.getEnrollment().orElse(null), that.getEnrollment().orElse(null));
        delta.addValueIfChanged(PropertyType.MAX_ENROLLED,
                this.getMaxEnrollment().orElse(null), that.getMaxEnrollment().orElse(null));

        delta.addValueIfChanged(PropertyType.IS_CANCELLED, this.cancelled, that.cancelled);

        delta.addValueIfChanged(PropertyType.IS_ONLINE, this.online, that.online);
        delta.addValueIfChanged(PropertyType.IS_ALTERNATING, this.alternating, that.alternating);

        // Add added notes.
        that.notes.stream()
                .filter(x -> !this.notes.contains(x))
                .forEach(x -> delta.addAdded(PropertyType.NOTE, x));

        // Add removed notes.
        this.notes.stream()
                .filter(x -> !that.notes.contains(x))
                .forEach(x -> delta.addRemoved(PropertyType.NOTE, x));

        // Find removed one-time periods.
        this.oneTimePeriods.stream()
                .filter(x -> that.oneTimePeriods.stream()
                        .noneMatch(
                                y -> x.getTerm().equals(y.getTerm()) &&
                                        Objects.equals(x.getStartDateTime(), y.getStartDateTime()) &&
                                        Objects.equals(x.getEndDateTime(), y.getEndDateTime())
                        ))
                .forEach(x -> delta.addRemoved(PropertyType.ONE_TIME_PERIOD, x));

        // Find added one-time periods.
        that.oneTimePeriods.stream()
                .filter(x -> this.oneTimePeriods.stream()
                        .noneMatch(
                                y -> x.getTerm().equals(y.getTerm()) &&
                                        x.getStartDateTime().equals(y.getStartDateTime()) &&
                                        x.getEndDateTime().equals(y.getEndDateTime())
                        ))
                .forEach(x -> delta.addAdded(PropertyType.ONE_TIME_PERIOD, x));

        // Find changed one-time periods.
        for (OneTimePeriod thisOtp : this.oneTimePeriods) {
            for (OneTimePeriod thatOtp : that.oneTimePeriods) {
                boolean cond = thisOtp.getTerm().equals(thatOtp.getTerm()) &&
                        thisOtp.getStartDateTime().equals(thatOtp.getStartDateTime()) &&
                        Objects.equals(thisOtp.getEndDateTime(), thatOtp.getEndDateTime());
                if (cond) {
                    if (!thisOtp.equals(thatOtp))
                        delta.addSubstructureChange(thisOtp.findDifferences(thatOtp));
                    break;
                }
            }
        }

        // Find removed repeating periods.
        this.repeatingPeriods.stream()
                .filter(x -> that.repeatingPeriods.stream()
                        .noneMatch(
                                y -> x.getTerm().equals(y.getTerm())
                                        && x.getDayOfWeek().equals(y.getDayOfWeek())
                                        && x.getStartTime().equals(y.getStartTime())
                                        && x.getEndTime().equals(y.getEndTime())
                        ))
                .forEach(x -> delta.addRemoved(PropertyType.REPEATING_PERIOD, x));

        // Find added repeating periods.
        that.repeatingPeriods.stream()
                .filter(x -> this.repeatingPeriods.stream()
                        .noneMatch(
                                y -> x.getTerm().equals(y.getTerm())
                                        && x.getDayOfWeek().equals(y.getDayOfWeek())
                                        && x.getStartTime().equals(y.getStartTime())
                                        && x.getEndTime().equals(y.getEndTime())
                        )
                ).forEach(x -> delta.addAdded(PropertyType.REPEATING_PERIOD, x));

        // Find changed repeating periods.
        for (RepeatingPeriod thisRp : this.repeatingPeriods)
            for (RepeatingPeriod thatRp : that.repeatingPeriods) {
                boolean cond = thisRp.getTerm().equals(thatRp.getTerm())
                        && thisRp.getStartTime().equals(thatRp.getStartTime())
                        && thisRp.getEndTime().equals(thatRp.getEndTime())
                        && thisRp.getDayOfWeek().equals(thatRp.getDayOfWeek());
                if (cond) {
                    if (!thisRp.equals(thatRp))
                        delta.addSubstructureChange(thisRp.findDifferences(thatRp));
                    break;
                }
            }

        return delta;
    }

    @Override
    public int compareTo(@Nonnull Section that) {
        if (this.sectionId.equals(that.sectionId)){
            return this.sectionId.compareTo(that.sectionId);
        }
        return this.equals(that) ? 0 : -1;
    }
}