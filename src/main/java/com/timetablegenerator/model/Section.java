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
    @NonNull @Getter private final String id;

    private Boolean waitingList;

    private Integer waiting;
    private Integer maxWaiting;

    private Boolean full;

    private Integer enrollment;
    private Integer maxEnrollment;

    private final Map<String, RepeatingPeriod> repeatingPeriods = new HashMap<>();
    private final Map<String, OneTimePeriod> oneTimePeriods = new HashMap<>();

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
            this.maxWaiting = null;
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
            if (this.maxEnrollment != null) {
                this.enrollment = this.maxEnrollment;
            }
            if (this.maxEnrollment == null && this.enrollment != null) {
                this.maxEnrollment = this.enrollment;
            }
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
        if (this.oneTimePeriods.putIfAbsent(period.getUniqueId(), period) != null) {
            throw new IllegalArgumentException("A one-time period '" + period.getUniqueId()
                    + "' is already part of this section.");
        }
        return this;
    }

    public Section addPeriod(RepeatingPeriod period){
        if (this.repeatingPeriods.putIfAbsent(period.getUniqueId(), period) != null) {
            throw new IllegalArgumentException("A repeating period '" + period.getUniqueId()
                    + "' is already part of this section.");
        }
        return this;
    }

    public Collection<RepeatingPeriod> getRepeatingPeriods() {
        return new TreeSet<>(this.repeatingPeriods.values());
    }

    public Collection<OneTimePeriod> getOneTimePeriods() {
        return new TreeSet<>(this.oneTimePeriods.values());
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(this.id);

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
            this.repeatingPeriods.values().stream().sorted().forEach(x -> sb.append("\n\n")
                    .append(StringUtilities.indent(2, x.toString())));
        }

        if (!this.oneTimePeriods.isEmpty()) {
            sb.append("\n\n").append(I).append("One time periods:");
            this.oneTimePeriods.values().stream().sorted().forEach(x -> sb.append("\n\n")
                    .append(StringUtilities.indent(2, x.toString())));
        }

        return sb.toString();
    }

    @Override
    public String getDeltaId(){
        return this.getId();
    }

    @Override
    public StructureDelta findDifferences(@NonNull Section that) {

        if (!this.id.equals(that.id)) {
            throw new IllegalArgumentException("Sections are not related: \"" + this.id
                    + "\" and \"" + that.id + "\"");
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
        this.oneTimePeriods.entrySet().stream()
                .filter(x -> !that.oneTimePeriods.containsKey(x.getKey()))
                .forEach(x -> delta.addRemoved(PropertyType.ONE_TIME_PERIOD, x.getValue()));

        // Find added one-time periods.
        that.oneTimePeriods.entrySet().stream()
                .filter(x -> !this.oneTimePeriods.containsKey(x.getKey()))
                .forEach(x -> delta.addAdded(PropertyType.ONE_TIME_PERIOD, x.getValue()));

        // Find changed one-time periods.
        Set<String> samePeriods = new HashSet<>(this.oneTimePeriods.keySet());
        samePeriods.retainAll(that.oneTimePeriods.keySet());

        for (String key : samePeriods) {
            OneTimePeriod thisOtp = this.oneTimePeriods.get(key);
            OneTimePeriod thatOtp = that.oneTimePeriods.get(key);
            if (!thisOtp.equals(thatOtp)) {
                delta.addSubstructureChange(thisOtp.findDifferences(thatOtp));
            }
        }

        // Find removed repeating periods.
        this.repeatingPeriods.entrySet().stream()
                .filter(x -> !that.repeatingPeriods.containsKey(x.getKey()))
                .forEach(x -> delta.addRemoved(PropertyType.REPEATING_PERIOD, x.getValue()));

        // Find added repeating periods.
        that.repeatingPeriods.entrySet().stream()
                .filter(x -> !this.repeatingPeriods.containsKey(x.getKey()))
                .forEach(x -> delta.addAdded(PropertyType.REPEATING_PERIOD, x.getValue()));

        // Find changed repeating periods.
        samePeriods = new HashSet<>(this.repeatingPeriods.keySet());
        samePeriods.retainAll(that.repeatingPeriods.keySet());

        for (String key : samePeriods) {
            RepeatingPeriod thisRp = this.repeatingPeriods.get(key);
            RepeatingPeriod thatRp = that.repeatingPeriods.get(key);
            if (!thisRp.equals(thatRp)) {
                delta.addSubstructureChange(thisRp.findDifferences(thatRp));
            }
        }

        return delta;
    }

    @Override
    public int compareTo(@Nonnull Section that) {
        if (!this.id.equals(that.id)){
            return this.id.compareTo(that.id);
        }
        // Some standard classes will use this instead of .equals() so ensure non-zero if not equal.
        return this.equals(that) ? 0 : -1;
    }
}