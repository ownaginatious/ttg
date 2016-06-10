package com.timetablegenerator.model;

import com.timetablegenerator.Settings;
import com.timetablegenerator.StringUtilities;
import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import java.util.*;

@EqualsAndHashCode
@Accessors(chain = true)
public class Course implements Comparable<Course>, Diffable<Course> {

    private static final String I = Settings.getIndent();

    // Required data.
    @Getter private final School school;
    @Getter private final String code;
    @Getter private final String name;
    @Getter private final String uniqueId;
    @Getter private final Department department;
    @Getter private final Term term;

    // Optional data.
    @Setter private String description = null;
    @Setter private Double credits = null;
    private final List<String> notes = new ArrayList<>();

    private final Set<Course> crossListings = new HashSet<>();
    private final Set<Course> prerequisites = new HashSet<>();
    private final Set<Course> antirequisites = new HashSet<>();
    private final Set<Course> corequisites = new HashSet<>();

    private final Map<String, SectionType> sectionTypes = new HashMap<>();

    private Course(School school, Term term, Department department,
                   String courseCode, String courseName) {

        // Null check most parameters to the function.
        this.school = school;
        this.term = term;
        this.department = department;
        this.code = courseCode;
        this.name = courseName;

        // Generate the unique identifier for this course.
        this.uniqueId = this.department.getCode() + this.code + this.term.getTermDefinition().getCode();
    }

    /**
     * Creates a new course object representing a single course within a timetable.
     *
     * @param school     The school for the school's terms.
     * @param term       The term this course is part of.
     * @param department The department to which this course belongs.
     * @param code       The code of this course.
     * @param name       The name of this course.
     */
    public static Course of(@NonNull School school, @NonNull Term term,
                            @NonNull Department department, @NonNull String code,
                            @NonNull String name) {
        return new Course(school, term, department, code, name);
    }

    private void checkRelationalIntegrity(Set<Course> collection, Course course) {
        if (course == this) {
            throw new IllegalArgumentException("A course cannot be related to itself");
        }
        boolean ok = (collection == this.prerequisites || !this.prerequisites.contains(course)) &&
                (collection == this.antirequisites || !this.antirequisites.contains(course)) &&
                (collection == this.crossListings || !this.crossListings.contains(course)) &&
                (collection == this.corequisites || !this.corequisites.contains(course));
        if (!ok) {
            throw new IllegalArgumentException(
                    String.format("Course %s cannot be under multiple relation categories", course.getUniqueId()));
        }
    }

    public Course addPrerequisite(Course c) {
        this.checkRelationalIntegrity(this.prerequisites, c);
        this.prerequisites.add(c);
        return this;
    }

    public Course addAntirequisite(Course c) {
        this.checkRelationalIntegrity(this.antirequisites, c);
        this.antirequisites.add(c);
        return this;
    }

    public Course addCorequesite(Course c) {
        this.checkRelationalIntegrity(this.corequisites, c);
        this.corequisites.add(c);
        return this;
    }

    public Course addCrossListing(Course c) {
        this.checkRelationalIntegrity(this.crossListings, c);
        this.crossListings.add(c);
        return this;
    }

    public Course addNotes(@NonNull Collection<String> notes) {
        this.notes.addAll(notes);
        return this;
    }

    public Course addNotes(@NonNull String... notes) {
        Collections.addAll(this.notes, notes);
        return this;
    }

    public Optional<Double> getCredits() {
        return this.credits == null ? Optional.empty() : Optional.of(this.credits);
    }

    public Optional<String> getDescription() {
        return this.description == null ? Optional.empty() : Optional.of(this.description);
    }

    public List<String> getNotes() {
        return Collections.unmodifiableList(this.notes);
    }

    public Collection<Course> getCrossListings() {
        return Collections.unmodifiableSet(this.crossListings);
    }

    public Collection<Course> getPrerequisites() {
        return Collections.unmodifiableSet(this.prerequisites);
    }

    public Collection<Course> getAntirequisites() {
        return Collections.unmodifiableSet(this.antirequisites);
    }

    public Collection<Course> getCorequisites() {
        return new HashSet<>(this.corequisites);
    }

    public Collection<String> getSectionTypes() {
        return this.sectionTypes.keySet();
    }

    public Optional<SectionType> getSectionType(String type) {
        return this.sectionTypes.containsKey(type) ? Optional.of(this.sectionTypes.get(type)) : Optional.empty();
    }

    public Course addSection(String sectionTypeId, Section s) {
        SectionType st = sectionTypes.get(sectionTypeId);
        if (st == null) {
            st = SectionType.of(this.school, sectionTypeId);
            sectionTypes.put(sectionTypeId, st);
        }
        st.addSection(s);
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("* Course: ").append(this.name)
                .append(" [").append(this.code).append("]");

        sb.append("\n* Department: ").append(this.department);
        sb.append("\n* Term: ").append(this.term);
        this.getCredits().ifPresent(x -> sb.append("\n* Credits: ").append(x));

        if (!this.crossListings.isEmpty()) {
            sb.append("\n* Cross-listings: ")
                    .append(this.crossListings.stream()
                            .map(Course::getUniqueId).sorted()
                            .collect(Collectors.joining(", ")));
        }
        if (!this.prerequisites.isEmpty()) {
            sb.append("\n* Pre-requisites: ")
                    .append(this.prerequisites.stream()
                            .map(Course::getUniqueId).sorted()
                            .collect(Collectors.joining(", ")));
        }
        if (!this.antirequisites.isEmpty()) {
            sb.append("\n* Anti-requisites: ")
                    .append(this.antirequisites.stream()
                            .map(Course::getUniqueId).sorted()
                            .collect(Collectors.joining(", ")));
        }
        if (!this.corequisites.isEmpty()) {
            sb.append("\n* Co-requisites: ")
                    .append(this.corequisites.stream()
                            .map(Course::getUniqueId).sorted()
                            .collect(Collectors.joining(", ")));
        }

        if (!this.notes.isEmpty()) {
            sb.append("\n* Notes:\n");
            this.notes.forEach(x -> sb.append('\n').append(StringUtilities.indent(1, x)));
            sb.append('\n');
        }

        sb.append("\n* Sections:");

        if (this.sectionTypes.isEmpty()) {
            sb.append("\n\n").append(I).append("NONE");
        } else {
            this.sectionTypes.values().forEach(x -> sb.append("\n\n")
                    .append(StringUtilities.indent(1, x.toString())));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull Course that) {
        if (!this.department.equals(that.department)) {
            return this.department.compareTo(that.department);
        }
        if (!this.code.equals(that.code)) {
            return this.code.compareTo(that.code);
        }
        if (!this.name.equals(that.name)) {
            return this.name.compareTo(that.name);
        }
        return this.equals(that) ? 0 : -1;
    }

    @Override
    public String getDeltaId() {
        return this.uniqueId;
    }

    @Override
    public StructureChangeDelta findDifferences(Course that) {

        if (!this.uniqueId.equals(that.uniqueId) || !Objects.equals(this.school, that.school)) {
            throw new IllegalArgumentException("Courses are not related: \"" + this.uniqueId
                    + "\" and \"" + that.uniqueId + "\"");
        }

        final StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.COURSE, this);

        delta.addValueIfChanged(PropertyType.NAME, this.name, that.name);
        delta.addValueIfChanged(PropertyType.DESCRIPTION, this.description, that.description);
        delta.addValueIfChanged(PropertyType.CREDITS, this.credits, that.credits);

        // Add added notes.
        that.notes.stream()
                .filter(x -> !this.notes.contains(x))
                .forEach(x -> delta.addAdded(PropertyType.NOTE, x));

        // Add removed notes.
        this.notes.stream()
                .filter(x -> !that.notes.contains(x))
                .forEach(x -> delta.addRemoved(PropertyType.NOTE, x));

        recordCourseRelationDiff(delta, PropertyType.CROSS_LISTING, this.crossListings, that.crossListings);
        recordCourseRelationDiff(delta, PropertyType.PRE_REQUISITE, this.prerequisites, that.prerequisites);
        recordCourseRelationDiff(delta, PropertyType.ANTI_REQUISITE, this.antirequisites, that.antirequisites);
        recordCourseRelationDiff(delta, PropertyType.CO_REQUISITE, this.corequisites, that.corequisites);

        Set<String> sectionTypeKeys = new HashSet<>(this.sectionTypes.keySet());
        sectionTypeKeys.addAll(that.sectionTypes.keySet());

        // Add added section types.
        sectionTypeKeys.stream()
                .filter(x -> !this.sectionTypes.containsKey(x) && that.sectionTypes.containsKey(x))
                .forEach(x -> delta.addAdded(PropertyType.SECTION_TYPE, that.sectionTypes.get(x)));

        // Add removed section types.
        sectionTypeKeys.stream()
                .filter(x -> this.sectionTypes.containsKey(x) && !that.sectionTypes.containsKey(x))
                .forEach(x -> delta.addRemoved(PropertyType.SECTION_TYPE, this.sectionTypes.get(x)));

        // Add changed section types.
        sectionTypeKeys.stream()
                .filter(x -> this.sectionTypes.containsKey(x) && that.sectionTypes.containsKey(x))
                .filter(x -> !this.sectionTypes.get(x).equals(that.sectionTypes.get(x)))
                .forEach(x -> delta.addChange(this.sectionTypes.get(x).findDifferences(that.sectionTypes.get(x))));

        return delta;
    }

    private static Set<String> shallowCourseSet(Collection<Course> courses) {
        return courses.stream().map(Course::getUniqueId).collect(Collectors.toSet());
    }

    private static void recordCourseRelationDiff(StructureChangeDelta delta, PropertyType propertyType,
                                                 Collection<Course> thisListing, Collection<Course> thatListing) {

        Set<String> removed = shallowCourseSet(thisListing);
        Set<String> added = shallowCourseSet(thatListing);
        Set<String> overlap = new HashSet<>(added);
        overlap.retainAll(removed);

        added.stream().filter(x -> !overlap.contains(x))
                .forEach(x -> delta.addAdded(propertyType, x));
        removed.stream().filter(x -> !overlap.contains(x))
                .forEach(x -> delta.addRemoved(propertyType, x));
    }
}
