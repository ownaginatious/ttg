package com.timetablegenerator.model.period;

import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import com.timetablegenerator.model.TermClassifier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@Accessors(chain = true)
public abstract class Period {

    @Getter protected final TermClassifier term;
    @Getter protected final Set<String> supervisors = new TreeSet<>();
    @Setter protected String room;
    @Setter protected String campus;

    @Getter protected final List<String> notes = new ArrayList<>();

    @Setter protected Boolean online;

    Period(TermClassifier term) {

        if (term == null)
            throw new IllegalStateException("Attempted to set null term into repeating time period.");

        this.term = term;
    }

    public Optional<Boolean> isOnline(){
        return this.online == null ? Optional.empty() : Optional.of(this.online);
    }

    public Period addSupervisors(String... supervisors){

        Collections.addAll(this.supervisors, supervisors);
        return this;
    }

    public Period addSupervisors(Collection<String> supervisors){

        this.supervisors.addAll(supervisors);
        return this;
    }

    public Period addNotes(String... note){

        Collections.addAll(this.notes, note);
        return this;
    }

    public Period addNotes(Collection<String> notes){

        this.notes.addAll(notes);
        return this;
    }

    public Optional<String> getRoom(){
        return this.room == null ? Optional.empty(): Optional.of(this.room);
    }

    public Optional<String> getCampus(){
        return this.campus == null ? Optional.empty() : Optional.of(this.campus);
    }

    public abstract boolean isScheduled();

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (o == null || this.getClass() != o.getClass())
            return false;

        Period that = (Period) o;

        return Objects.equals(this.room, that.room)
                && this.term.equals(that.term);
    }

    @Override
    public int hashCode() {
        return 31 * term.hashCode() + (room != null ? room.hashCode() : 0);
    }

    protected void savePeriodDifferences(StructureChangeDelta delta, Period that) {

        delta.addIfChanged(PropertyType.TERM, this.term, that.term);
        delta.addIfChanged(PropertyType.ROOM, this.room, that.room);
        delta.addIfChanged(PropertyType.CAMPUS, this.campus, that.campus);
        delta.addIfChanged(PropertyType.IS_ONLINE, this.online, that.online);

        // Add added notes.
        that.notes.stream()
                .filter(x -> !this.notes.contains(x))
                .forEach(x -> delta.addAdded(PropertyType.NOTE, x));

        // Add removed notes.
        this.notes.stream()
                .filter(x -> !that.notes.contains(x))
                .forEach(x -> delta.addRemoved(PropertyType.NOTE, x));

        // Add added notes.
        that.supervisors.stream()
                .filter(x -> !this.supervisors.contains(x))
                .forEach(x -> delta.addAdded(PropertyType.SUPERVISOR, x));

        // Add removed notes.
        this.supervisors.stream()
                .filter(x -> !that.supervisors.contains(x))
                .forEach(x -> delta.addRemoved(PropertyType.SUPERVISOR, x));
    }
}