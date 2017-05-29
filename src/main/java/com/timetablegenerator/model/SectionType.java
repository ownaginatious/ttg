package com.timetablegenerator.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureDelta;
import lombok.*;

import javax.annotation.Nonnull;
import java.util.*;

@EqualsAndHashCode
public class SectionType implements Diffable<SectionType> {

    private static final String I = Settings.getIndent();

    @NonNull @Getter private final School school;
    @NonNull @Getter private final Term term;
    @NonNull @Getter private final String code;
    @NonNull @Getter private final String name;

    private final Map<String, Section> sections = new TreeMap<>();

    private SectionType(@NonNull School school, @NonNull Term term, @NonNull String code, @NonNull String name) {
        this.school = school;
        this.term = term;
        this.code = code;
        this.name = name;
    }

    public static SectionType of(@NonNull School school, @NonNull Term term, @NonNull String code){
        String name = school.getSectionTypeName(code);
        return new SectionType(school, term, code, name);
    }

    public Collection<Section> getSections(){
        // TreeMap<>.values apparently doesn't have .equals(...) implemented,
        // which can lead to false negatives when doing equality checks.
        // Therefore, we wrap it in an array list to materialize the collection.
        return new ArrayList<>(this.sections.values());
    }

    public Optional<Section> getSection(String sectionId) {
        return this.sections.containsKey(sectionId) ?
                Optional.of(this.sections.get(sectionId)) : Optional.empty();
    }

    public SectionType addSection(@NonNull Section s) {
        s.getTerm().assertFallsWithin(this.term);
        if (this.sections.putIfAbsent(s.getId(), s) != null) {
            throw new IllegalStateException("Attempted to add the section of code \""
                    + this.code + "\" with ID \"" + s.getId() + "\" twice");
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
        // Some standard classes will use this instead of .equals() so ensure non-zero if not equal.
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String getDeltaId(){
        return this.code;
    }

    @Override
    public StructureDelta findDifferences(@NonNull SectionType that) {

        if (!this.code.equals(that.code)) {
            throw new IllegalArgumentException("Section types are not related: \"" + this.code
                    + "\" and \"" + that.code + "\"");
        }
        if (!this.term.temporallyEquals(that.term)) {
            throw new IllegalArgumentException("Section types are in unrelated terms: \"" +
                    this.term.getUniqueId() + "\" and \"" + that.term.getUniqueId() + "\"");
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
