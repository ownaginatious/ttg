package com.timetablegenerator.model;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.period.Period;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.model.period.OneTimePeriod;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Section implements Diffable<Section> {

    private String serialNumber;
    private final String sectionId;

    private Boolean waitingList;

    private int waiting = -1;
    private int maxWaiting = -1;

    private Boolean full;

    private int enrollment = -1;
    private int maxEnrollment = -1;

    private Boolean cancelled = null;

    private final Set<RepeatingPeriod> repeatingPeriods = new TreeSet<>();
    private final Set<OneTimePeriod> oneTimePeriods = new TreeSet<>();

    private final List<String> notes = new ArrayList<>();

    private Boolean online = null;
    private Boolean alternating = null;

    private Section(@Nonnull String sectionId) {
        this.sectionId = sectionId;
    }

    public static Section fromName(String sectionId) {
        return new Section(sectionId);
    }

    public String getId() {
        return this.sectionId;
    }

    public Optional<String> getSerialNumber() {
        return this.serialNumber == null ? Optional.empty() : Optional.of(this.serialNumber);
    }

    public Section addNotes(@Nonnull String note) {
        this.notes.add(note);
        return this;
    }

    public List<String> getNotes() {
        return new ArrayList<>(this.notes);
    }

    public Section setOnline(Boolean online) {

        this.online = online;
        return this;
    }

    public Optional<Boolean> isOnline() {
        return this.online == null ? Optional.empty() : Optional.of(this.online);
    }

    public Section setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    public Optional<Boolean> isCancelled() {
        return this.cancelled == null ? Optional.empty() : Optional.of(this.cancelled);
    }

    public Section setAlternating(Boolean alternating) {

        this.alternating = alternating;
        return this;
    }

    public Optional<Boolean> isAlternating() {
        return this.alternating == null ? Optional.empty() : Optional.of(this.alternating);
    }

    public Section setSerialNumber(@Nonnull String serialNumber) {

        this.serialNumber = serialNumber;
        return this;
    }

    public Section setWaitingList(Boolean waitingList) {

        this.waitingList = waitingList;

        if (this.waitingList == null || !this.waitingList)
            this.waiting = -1;

        return this;
    }

    public Optional<Boolean> hasWaitingList() {
        return this.waitingList == null ? Optional.empty() : Optional.of(this.waitingList);
    }

    public Optional<Integer> getWaiting() {
        return this.waiting == -1 ? Optional.empty() : Optional.of(this.waiting);
    }

    public Section setWaiting(int waiting) {

        if (waiting < 0)
            throw new IllegalStateException("Waiting number must be greater than or equal to 0 (" + waiting + ")");
        else if (this.maxWaiting >= 0 && waiting > this.maxWaiting)
            throw new IllegalStateException("Waiting number must be less than the maximum ("
                    + waiting + "/" + this.maxWaiting + ")");

        this.waiting = waiting;
        this.waitingList = true;
        return this;
    }

    public Optional<Integer> getMaxWaiting() {
        return this.maxWaiting == -1 ? Optional.empty() : Optional.of(this.maxWaiting);
    }

    public Section setMaximumWaiting(int maxWaiting) {

        if (maxWaiting < 0)
            throw new IllegalStateException("Maximum number of people waiting must be greater than or equal to 0 ("
                    + maxWaiting + ")");
        else if (this.waiting >= 0 && this.waiting > maxWaiting)
            throw new IllegalStateException("Number of people waiting must be less than or equal to the maximum ("
                    + this.waiting + "/" + maxWaiting + ")");

        this.maxWaiting = maxWaiting;
        return this;
    }

    public Section setFull(Boolean full) {

        this.full = full;

        if (this.full == null || !this.full)
            this.enrollment = -1;

        return this;
    }

    public Optional<Boolean> isFull() {
        return this.full == null ? Optional.empty() : Optional.of(this.full);
    }

    public Optional<Integer> getEnrollment() {
        return this.enrollment == -1 ? Optional.empty() : Optional.of(enrollment);
    }

    public Section setEnrollment(int enrollment) {

        if (enrollment < 0)
            throw new IllegalStateException("Enrollment must be greater than or equal to 0 (" + enrollment + ")");
        else if (this.maxEnrollment >= 0 && enrollment > this.maxEnrollment)
            throw new IllegalStateException("Number of people enrolled must be less than the maximum ("
                    + enrollment + "/" + this.maxEnrollment + ")");

        this.enrollment = enrollment;

        if (this.maxEnrollment >= 0)
            this.full = this.enrollment == this.maxEnrollment;

        return this;
    }

    public Optional<Integer> getMaxEnrollment() {
        return this.maxEnrollment == -1 ? Optional.empty() : Optional.of(this.maxEnrollment);
    }

    public Section setMaximumEnrollment(int maxEnrollment) {

        if (maxEnrollment < 0)
            throw new IllegalStateException("Maximum number of people enrolled must be greater than or equal to 0 ("
                    + maxWaiting + ")");
        else if (this.waiting >= 0 && this.waiting > maxWaiting)
            throw new IllegalStateException("Number of people waiting must be less than the maximum ("
                    + waiting + "/" + maxWaiting + ")");

        this.maxEnrollment = maxEnrollment;

        if (this.enrollment >= 0)
            this.full = this.enrollment == this.maxEnrollment;

        return this;
    }

    public Section addPeriod(Period dayTime) {

        if (dayTime instanceof OneTimePeriod)
            this.oneTimePeriods.add((OneTimePeriod) dayTime);
        else
            this.repeatingPeriods.add((RepeatingPeriod) dayTime);

        return this;
    }

    public Collection<RepeatingPeriod> getRepeatingPeriods() {
        return new TreeSet<>(this.repeatingPeriods);
    }

    public Collection<OneTimePeriod> getOneTimePeriods() {
        return new TreeSet<>(this.oneTimePeriods);
    }

    @Override
    public boolean equals(Object e) {

        if (!(e instanceof Section))
            return false;

        Section that = (Section) e;

        return Objects.equals(this.sectionId, that.sectionId)
                && Objects.equals(this.repeatingPeriods, that.repeatingPeriods)
                && Objects.equals(this.oneTimePeriods, that.oneTimePeriods)
                && this.alternating == that.alternating;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int tabAmount) {

        String preTabs = IntStream.rangeClosed(1, tabAmount).mapToObj(x -> "\t").collect(Collectors.joining());

        StringBuilder sb = new StringBuilder("\n");

        sb.append(preTabs).append(this.sectionId);

        if (this.serialNumber != null)
            sb.append(" {").append(this.serialNumber).append('}');

        if (this.cancelled != null && this.cancelled)
            return sb.append(" [CANCELLED]\n").toString();

        if (this.full != null) {

            if (this.full)
                sb.append(" [FULL]");
            else
                sb.append(" [AVAILABLE]");
        }

        if (this.enrollment > -1 || this.maxEnrollment > -1) {

            sb.append(" [enrolled: ")
                    .append(this.enrollment > -1 ? this.enrollment : "?")
                    .append('/')
                    .append(this.maxEnrollment > -1 ? this.maxEnrollment : "?")
                    .append(" ]");
        }

        if (this.waitingList != null) {

            sb.append(" [waiting: ")
                    .append(this.waiting > -1 ? this.waiting : "?")
                    .append('/')
                    .append(this.maxWaiting > -1 ? this.maxWaiting : "?")
                    .append(" ]");
        }

        sb.append('\n');

        if (!this.repeatingPeriods.isEmpty()) {

            sb.append("\n").append(preTabs).append("\tRepeating periods:\n").append(preTabs).append('\n');

            for (RepeatingPeriod rp : this.repeatingPeriods)
                sb.append(preTabs).append("\t\t").append(rp).append('\n');
        }

        if (!this.oneTimePeriods.isEmpty()) {

            sb.append("\n").append(preTabs).append("\tOne time periods:\n").append(preTabs).append('\n');

            for (OneTimePeriod sp : this.oneTimePeriods)
                sb.append(preTabs).append("\t\t").append(sp).append('\n');
        }

        return sb.toString();
    }

    @Override
    public StructureChangeDelta findDifferences(Section that) {

        if (!this.sectionId.equals(that.sectionId)) {
            throw new IllegalArgumentException("Sections are not related: \"" + this.sectionId
                    + "\" and \"" + that.sectionId + "\"");
        }

        final StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.SECTION, this.sectionId);

        delta.addIfChanged(PropertyType.SERIAL, this.serialNumber, that.serialNumber);

        delta.addIfChanged(PropertyType.WAITING_LIST, this.waitingList, that.waitingList);
        delta.addIfChanged(PropertyType.NUM_WAITING, this.waiting, that.waiting);
        delta.addIfChanged(PropertyType.MAX_WAITING, this.maxWaiting, that.maxWaiting);

        delta.addIfChanged(PropertyType.IS_FULL, this.full, that.full);
        delta.addIfChanged(PropertyType.NUM_ENROLLED, this.enrollment, that.enrollment);
        delta.addIfChanged(PropertyType.MAX_ENROLLED, this.maxEnrollment, that.maxEnrollment);

        delta.addIfChanged(PropertyType.IS_CANCELLED, this.cancelled, that.cancelled);

        delta.addIfChanged(PropertyType.IS_ONLINE, this.online, that.online);
        delta.addIfChanged(PropertyType.IS_ALTERNATING, this.alternating, that.alternating);

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
                                y -> Objects.equals(x.getStartDateTime(), y.getStartDateTime())
                                        && Objects.equals(x.getEndDateTime(), y.getEndDateTime())
                        ))
                .forEach(x -> delta.addRemoved(PropertyType.SINGLE_PERIOD, x));

        // Find added one-time periods.
        that.oneTimePeriods.stream()
                .filter(x -> this.oneTimePeriods.stream()
                        .noneMatch(
                                y -> x.getStartDateTime().equals(y.getStartDateTime())
                                        && x.getEndDateTime().equals(y.getEndDateTime())
                        ))
                .forEach(x -> delta.addAdded(PropertyType.SINGLE_PERIOD, x));

        // Find changed one-time periods.
        for (OneTimePeriod thisOtp : this.oneTimePeriods)
            for (OneTimePeriod thatOtp : that.oneTimePeriods) {
                boolean cond = thisOtp.getStartDateTime().equals(thatOtp.getStartDateTime())
                        && Objects.equals(thisOtp.getEndDateTime(), thatOtp.getEndDateTime());
                if (cond) {
                    if (!thisOtp.equals(thatOtp))
                        delta.addChange(thisOtp.findDifferences(thatOtp));
                    break;
                }
            }

        // Find removed repeating periods.
        this.repeatingPeriods.stream()
                .filter(x -> that.repeatingPeriods.stream()
                        .noneMatch(
                                y -> x.getDayOfWeek().equals(y.getDayOfWeek())
                                        && x.getStartTime().equals(y.getStartTime())
                                        && x.getEndTime().equals(y.getEndTime())
                        ))
                .forEach(x -> delta.addRemoved(PropertyType.REPEATING_PERIOD, x));

        // Find added repeating periods.
        that.repeatingPeriods.stream()
                .filter(x -> this.repeatingPeriods.stream()
                        .noneMatch(
                                y -> x.getDayOfWeek().equals(y.getDayOfWeek())
                                        && x.getStartTime().equals(y.getStartTime())
                                        && x.getEndTime().equals(y.getEndTime())
                        )
                ).forEach(x -> delta.addAdded(PropertyType.REPEATING_PERIOD, x));

        // Find changed repeating periods.
        for (RepeatingPeriod thisRp : this.repeatingPeriods)
            for (RepeatingPeriod thatRp : that.repeatingPeriods) {
                boolean cond = thisRp.getStartTime().equals(thatRp.getStartTime())
                        && Objects.equals(thisRp.getEndTime(), thatRp.getEndTime());
                if (cond) {
                    if (!thisRp.equals(thatRp))
                        delta.addChange(thisRp.findDifferences(thatRp));
                    break;
                }
            }

        return delta;
    }
}