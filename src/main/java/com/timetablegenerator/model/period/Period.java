package com.timetablegenerator.model.period;

import com.timetablegenerator.Settings;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

@Accessors(chain = true)
@EqualsAndHashCode()
abstract class Period {

    protected static final String I = Settings.getIndent();

    @Getter private final TermClassifier term;
    private final Set<String> supervisors = new TreeSet<>();
    @Setter private String room;
    @Setter private String campus;

    private final List<String> notes = new ArrayList<>();

    @Setter private Boolean online;

    Period(@NonNull TermClassifier term) {
        this.term = term;
    }

    public Optional<Boolean> isOnline(){
        return this.online == null ? Optional.empty() : Optional.of(this.online);
    }

    public Period addSupervisors(String... supervisors){

        Collections.addAll(this.supervisors, supervisors);
        return this;
    }

    public Set<String> getSupervisors(){
        return Collections.unmodifiableSet(this.supervisors);
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

    public List<String> getNotes(){
        return Collections.unmodifiableList(this.notes);
    }

    public Optional<String> getRoom(){
        return this.room == null ? Optional.empty(): Optional.of(this.room);
    }

    public Optional<String> getCampus(){
        return this.campus == null ? Optional.empty() : Optional.of(this.campus);
    }

    public abstract boolean isScheduled();

    protected void savePeriodDifferences(StructureChangeDelta delta, Period that) {

        delta.addValueIfChanged(PropertyType.ROOM, this.room, that.room);
        delta.addValueIfChanged(PropertyType.CAMPUS, this.campus, that.campus);
        delta.addValueIfChanged(PropertyType.IS_ONLINE, this.online, that.online);

        // Add added notes.
        that.notes.stream()
                .filter(x -> !this.notes.contains(x))
                .forEach(x -> delta.addAdded(PropertyType.NOTE, x));

        // Add removed notes.
        this.notes.stream()
                .filter(x -> !that.notes.contains(x))
                .forEach(x -> delta.addRemoved(PropertyType.NOTE, x));


        // Add added supervisors.
        that.supervisors.stream()
                .filter(x -> !this.supervisors.contains(x))
                .forEach(x -> delta.addAdded(PropertyType.SUPERVISOR, x));

        // Add removed supervisors.
        this.supervisors.stream()
                .filter(x -> !that.supervisors.contains(x))
                .forEach(x -> delta.addRemoved(PropertyType.SUPERVISOR, x));
    }
}