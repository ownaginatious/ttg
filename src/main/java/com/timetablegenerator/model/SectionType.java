package com.timetablegenerator.model;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SectionType implements Comparable<SectionType>, Diffable<SectionType> {

    @Getter private final School school;
    @Getter private final String type;
    private final Map<String, Section> sections = new TreeMap<>();

    public SectionType(@Nonnull School school, @Nonnull String type) {

        // Force check to ensure the type exists.
        school.getSectionTypeNameByCode(type);

        this.school = school;
        this.type = type;
    }

    public Set<String> getSectionKeys(){
        return this.sections.keySet();
    }

    public Section getSection(String sectionId) {
        return this.sections.get(sectionId);
    }

    public void addSection(@Nonnull Section s) {

        if (this.sections.putIfAbsent(s.getSectionId(), s) != null)
            throw new IllegalStateException("Attempted to add the section of type \""
                    + this.type + "\" with ID \"" + s.getSectionId() + "\" twice");
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("\n");

        sb.append(school.getSectionTypeNameByCode(this.type)).append(" data:\n");

        if (this.sections.isEmpty()){
            sb.append('\n').append("NONE LISTED");
        } else {
            this.sections.values().forEach(sb::append);
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull SectionType st) {
        return type.compareTo(st.type);
    }

    @Override
    public String getDeltaId(){
        return this.type;
    }

    @Override
    public StructureChangeDelta findDifferences(SectionType that) {

        if (!this.type.equals(that.type)) {
            throw new IllegalArgumentException("Section types are not related: \"" + this.type
                    + "\" and \"" + that.type + "\"");
        }

        final StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.SECTION_TYPE, this);

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
                .forEach(x -> delta.addChange(this.sections.get(x).findDifferences(that.sections.get(x))));

        return delta;
    }
}
