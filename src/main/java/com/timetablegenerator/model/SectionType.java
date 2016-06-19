package com.timetablegenerator.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.*;

@EqualsAndHashCode
public class SectionType implements Diffable<SectionType> {

    private static final String I = Settings.getIndent();

    @Getter private final School school;
    @Getter private final String code;
    @Getter private final String name;

    private final Map<String, Section> sections = new TreeMap<>();

    private SectionType(@NonNull School school, @NonNull String type) {
        this.school = school;
        this.code = type;
        this.name = school.getSectionTypeName(this.code);
    }

    public static SectionType of(@NonNull School school, @NonNull String type){
        // Force check to ensure the code exists.
        school.getSectionTypeName(type);
        return new SectionType(school, type);
    }

    public Set<String> getSectionKeys(){
        return this.sections.keySet();
    }

    public Optional<Section> getSection(String sectionId) {
        return this.sections.containsKey(sectionId) ?
                Optional.of(this.sections.get(sectionId)) : Optional.empty();
    }

    public SectionType addSection(@NonNull Section s) {
        if (this.sections.putIfAbsent(s.getSectionId(), s) != null) {
            throw new IllegalStateException("Attempted to add the section of code \""
                    + this.code + "\" with ID \"" + s.getSectionId() + "\" twice");
        }
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder(this.name)
                .append(" sections:");

        if (this.sections.isEmpty()){
            sb.append("\n\n").append(I).append("NONE LISTED");
        } else {
            this.sections.values().forEach(x -> sb.append("\n\n")
                    .append(StringUtilities.indent(1, x.toString())));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull SectionType that) {
        if (!this.code.equals(that.code)){
            return this.code.compareTo(that.code);
        } else if (!this.name.equals(that.name)){
            return this.name.compareTo(that.name);
        }
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String getDeltaId(){
        return this.code;
    }

    @Override
    public StructureDelta findDifferences(SectionType that) {

        if (!this.code.equals(that.code)) {
            throw new IllegalArgumentException("Section types are not related: \"" + this.code
                    + "\" and \"" + that.code + "\"");
        }

        final StructureDelta delta = StructureDelta.of(PropertyType.SECTION_TYPE, this);

        Set<String> sectionKeys = new HashSet<>(this.sections.keySet());
        sectionKeys.addAll(that.sections.keySet());

        // Add added sections.
        sectionKeys.stream()
                .filter(x -> that.sections.containsKey(x) && !this.sections.containsKey(x))
                .forEach(x -> delta.addAdded(PropertyType.SECTION, that.sections.get(x)));

        // Add removed sections.
        sectionKeys.stream()
                .filter(x -> !that.sections.containsKey(x) && this.sections.containsKey(x))
                .forEach(x -> delta.addRemoved(PropertyType.SECTION, this.sections.get(x)));

        // Add changed sections.
        sectionKeys.stream()
                .filter(x -> that.sections.containsKey(x) && this.sections.containsKey(x))
                .filter(x -> !that.sections.get(x).equals(this.sections.get(x)))
                .forEach(x -> delta.addSubstructureChange(this.sections.get(x).findDifferences(that.sections.get(x))));

        return delta;
    }
}
