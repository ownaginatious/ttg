package com.timetablegenerator.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.StructureDelta;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.model.period.OneTimePeriod;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nonnull;
import java.util.*;

@EqualsAndHashCode()
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(staticName = "of")
@Accessors(chain = true)
public class Section implements Diffable<Section> {

    private static final String I = Settings.getIndent();

    @NonNull @Setter private String serialNumber = null;
    @NonNull @Setter private String groupId = null;
    @NonNull @Getter private final String sectionId;

    private Boolean waitingList;

    private Integer waiting;
    private Integer maxWaiting;

    private Boolean full;

    private Integer enrollment;
    private Integer maxEnrollment;

    private final Set<RepeatingPeriod> repeatingPeriods = new TreeSet<>();
    private final Set<OneTimePeriod> oneTimePeriods = new TreeSet<>();

    private final List<String> notes = new ArrayList<>();

    @NonNull @Setter private Boolean cancelled = null;
    @NonNull @Setter private Boolean online = null;

    public Optional<String> getSerialNumber() {
        return Optional.ofNullable(this.serialNumber);
    }

    public Optional<String> getGroupId() {
        return Optional.ofNullable(this.groupId);
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
        return Optional.ofNullable(this.online);
    }

    public Optional<Boolean> isCancelled() {
        return Optional.ofNullable(this.cancelled);
    }

    public Section setWaitingList(boolean waitingList) {

        this.waitingList = waitingList;

        if (!this.waitingList) {
            this.waiting = null;
        }
        return this;
    }

    public Optional<Boolean> hasWaitingList() {
        return Optional.ofNullable(this.waitingList);
    }

    public Optional<Integer> getWaiting() {
        return Optional.ofNullable(this.waiting);
    }

    public Section setWaiting(int waiting) {
        if (waiting < 0) {
            throw new IllegalArgumentException("Waiting number must be greater than or equal to 0 (" + waiting + ")");
        } else if (this.maxWaiting != null && waiting > this.maxWaiting) {
            throw new IllegalArgumentException("Waiting number must be less than the maximum ("
                    + waiting + "/" + this.maxWaiting + ")");
        }
        this.waiting = waiting;
        this.waitingList = true;
        return this;
    }

    public Optional<Integer> getMaxWaiting() {
        return Optional.ofNullable(this.maxWaiting);
    }

    public Section setMaximumWaiting(int maxWaiting) {
        if (maxWaiting < 0) {
            throw new IllegalArgumentException("Maximum number of people waiting must be greater than or equal to 0 ("
                    + maxWaiting + ")");
        } else if (this.waiting != null && this.waiting > maxWaiting) {
            throw new IllegalArgumentException("Number of people waiting must be less than or equal to the maximum ("
                    + this.waiting + "/" + maxWaiting + ")");
        }
        this.maxWaiting = maxWaiting;
        return this;
    }

    public Section setFull(boolean full) {

        this.full = full;

        if (!this.full) {
            this.enrollment = null;
        } else {
            if (this.maxEnrollment != null)
                this.enrollment = this.maxEnrollment;
            if (this.maxEnrollment == null && this.enrollment != null)
                this.maxEnrollment = this.enrollment;
        }

        return this;
    }

    public Optional<Boolean> isFull() {
        return Optional.ofNullable(this.full);
    }

    public Optional<Integer> getEnrollment() {
        return Optional.ofNullable(this.enrollment);
    }

    public Section setEnrollment(int enrollment) {

        if (enrollment < 0) {
            throw new IllegalArgumentException("Enrollment must be greater than or equal to 0 (" + enrollment + ")");
        } else if (this.maxEnrollment != null && enrollment > this.maxEnrollment) {
            throw new IllegalArgumentException("Number of people enrolled must be less than the maximum ("
                    + enrollment + "/" + this.maxEnrollment + ")");
        }
        this.enrollment = enrollment;

        if (this.maxEnrollment != null) {
            this.full = this.enrollment.equals(this.maxEnrollment);
        }
        return this;
    }

    public Optional<Integer> getMaxEnrollment() {
        return Optional.ofNullable(this.maxEnrollment);
    }

    public Section setMaximumEnrollment(int maxEnrollment) {

        if (maxEnrollment < 0) {
            throw new IllegalArgumentException("Maximum number of people enrolled must be greater than or equal to 0 ("
                    + maxWaiting + ")");
        } else if (this.enrollment != null && this.enrollment > maxEnrollment) {
            throw new IllegalArgumentException("Number of people enrolled must be less than the maximum ("
                    + this.enrollment + "/" + maxEnrollment + ")");
        }

        this.maxEnrollment = maxEnrollment;

        if (this.enrollment != null) {
            this.full = this.enrollment.equals(this.maxEnrollment);
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
                .filter(x -> that.oneTimePeriods.stream().noneMatch(x::temporallyEquals))
                .forEach(x -> delta.addRemoved(PropertyType.ONE_TIME_PERIOD, x));

        // Find added one-time periods.
        that.oneTimePeriods.stream()
                .filter(x -> this.oneTimePeriods.stream().noneMatch(x::temporallyEquals)
                ).forEach(x -> delta.addAdded(PropertyType.ONE_TIME_PERIOD, x));

        // Find changed one-time periods.
        for (OneTimePeriod thisOtp : this.oneTimePeriods) {
            for (OneTimePeriod thatOtp : that.oneTimePeriods) {
                if (thisOtp.temporallyEquals(thatOtp)) {
                    if (!thisOtp.equals(thatOtp))
                        delta.addSubstructureChange(thisOtp.findDifferences(thatOtp));
                    break;
                }
            }
        }

        // Find removed repeating periods.
        this.repeatingPeriods.stream()
                .filter(x -> that.repeatingPeriods.stream().noneMatch(x::temporallyEquals)).
                forEach(x -> delta.addRemoved(PropertyType.REPEATING_PERIOD, x));

        // Find added repeating periods.
        that.repeatingPeriods.stream()
                .filter(x -> this.repeatingPeriods.stream().noneMatch(x::temporallyEquals))
                .forEach(x -> delta.addAdded(PropertyType.REPEATING_PERIOD, x));

        // Find changed repeating periods.
        for (RepeatingPeriod thisRp : this.repeatingPeriods)
            for (RepeatingPeriod thatRp : that.repeatingPeriods) {
                if (thisRp.temporallyEquals(thatRp)) {
                    if (!thisRp.equals(thatRp))
                        delta.addSubstructureChange(thisRp.findDifferences(thatRp));
                    break;
                }
            }

        return delta;
    }

    @Override
    public int compareTo(@Nonnull Section that) {
        if (!this.sectionId.equals(that.sectionId)){
            return this.sectionId.compareTo(that.sectionId);
        }
        // Some standard classes will use this instead of .equals() so ensure non-zero if not equal.
        return this.equals(that) ? 0 : -1;
    }
}