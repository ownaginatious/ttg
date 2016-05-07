package com.timetablegenerator.model;

import com.timetablegenerator.delta.Diffable;
import com.timetablegenerator.delta.PropertyType;
import com.timetablegenerator.delta.StructureChangeDelta;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@EqualsAndHashCode
@Accessors(chain = true)
public class Course implements Comparable<Course>, Diffable<Course> {

    // Required data.
    private final School school;
    @Getter private final String courseCode;
    @Getter private final String courseName;
    @Getter private final String uniqueId;
    @Getter private final Department department;
    @Getter private final TermClassifier term;
    @Getter private final double credits;

    // Optional data.
    @Getter @Setter private String description = null;
    private final List<String> notes = new ArrayList<>();

    private final Set<Course> crossListings = new HashSet<>();
    private final Set<Course> prerequisites = new HashSet<>();
    private final Set<Course> antirequisites = new HashSet<>();
    private final Set<Course> corequisites = new HashSet<>();

    private final Map<String, SectionType> sectionTypes = new HashMap<>();

    /**
     * Creates a new course object representing a single course within a timetable.
     *
     * @param school The school for the school's terms.
     * @param term          The term this course is part fromSectionId.
     * @param department    The department to which this course belongs.
     * @param courseCode    The code of this course.
     * @param courseName    The name of this course.
     * @param credits       The number of credits this course is worth.
     */
    public Course(@Nonnull School school, @Nonnull TermClassifier term,
                  @Nonnull Department department, @Nonnull String courseCode,
                  @Nullable String courseName, @Nonnull Double credits) {

        // Null check most parameters to the function.
        this.school = school;
        this.term = term;
        this.department = department;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;

        // Generate the unique identifier for this course.
        this.uniqueId = this.department.getCode() + this.courseCode + this.term.getId();
    }

    public Course addPrerequisite(Course c) {
        this.prerequisites.add(c);
        return this;
    }

    public Course addAntirequisite(Course c) {
        this.antirequisites.add(c);
        return this;
    }

    public Course addCorequesite(Course c){
        this.corequisites.add(c);
        return this;
    }

    public Course addCrossListing(Course c) {
        this.crossListings.add(c);
        return this;
    }

    public Course addNote(String note) {
        this.notes.add(note);
        return this;
    }

    public List<String> getNotes() {
        return new ArrayList<>(this.notes);
    }

    public Collection<Course> getCrossListings() {
        return new HashSet<>(this.crossListings);
    }

    public Collection<Course> getPrerequisites() {
        return new HashSet<>(this.prerequisites);
    }

    public Collection<Course> getAntirequisites() {
        return new HashSet<>(this.antirequisites);
    }

    public Collection<Course> getCorequisites() {
        return new HashSet<>(this.corequisites);
    }

    public SectionType getSectionType(String sectionTypeId) {
        return sectionTypes.get(sectionTypeId);
    }

    public Collection<String> getSectionTypes() {
        return this.sectionTypes.keySet();
    }

    public Course addSection(String sectionTypeId, Section s) {

        SectionType st = sectionTypes.get(sectionTypeId);

        if (st == null) {

            st = new SectionType(this.school, sectionTypeId);
            sectionTypes.put(sectionTypeId, st);
        }

        st.addSection(s);
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("* Course\t\t: ").append(this.courseName == null ? "Unknown course name" : this.courseName)
                .append(" [").append(this.courseCode).append("] (")
                .append(this.credits).append(" units)\n");

        sb.append("* Department\t: ").append(this.department).append('\n');
        sb.append("* Term\t\t\t: ").append(this.term.name()).append('\n');

        if (!this.notes.isEmpty()) {

            sb.append("* Notes:\n");

            int i = 0;

            for (String note : this.notes)
                sb.append("\t[").append(++i).append("] ").append(note);
        }

        if (!this.crossListings.isEmpty()) {
            sb.append("* Cross-listings: ")
                    .append(this.crossListings.stream()
                            .map(Course::getUniqueId)
                            .collect(Collectors.joining(", ")))
                    .append('\n');
        }
        if (!this.prerequisites.isEmpty()) {
            sb.append("* Pre-requisites: ")
                    .append(this.prerequisites.stream()
                            .map(Course::getUniqueId)
                            .collect(Collectors.joining(", ")))
                    .append('\n');
        }
        if (!this.antirequisites.isEmpty()) {
            sb.append("* Anti-requisites: ")
                    .append(this.antirequisites.stream()
                            .map(Course::getUniqueId)
                            .collect(Collectors.joining(", ")))
                    .append('\n');
        }

        sb.append("* Sections:\n");

        if (this.sectionTypes.isEmpty())
            sb.append('\t').append("NONE");
        else {
            this.sectionTypes.values().forEach(x -> sb.append('\n').append(x));
        }

        return sb.toString();
    }

    @Override
    public int compareTo(@Nonnull Course c) {

        int departmentComparison = this.department.compareTo(c.department);

        if (departmentComparison == 0)
            return this.courseCode.compareTo(c.courseCode);

        return departmentComparison;
    }

    @Override
    public String getDeltaId(){
        return this.uniqueId;
    }

    @Override
    public StructureChangeDelta findDifferences(Course that) {

        if (!this.uniqueId.equals(that.uniqueId)) {
            throw new IllegalArgumentException("Courses are not related: \"" + this.uniqueId
                    + "\" and \"" + that.uniqueId + "\"");
        }

        final StructureChangeDelta delta = StructureChangeDelta.of(PropertyType.COURSE, this);

        delta.addIfChanged(PropertyType.NAME, this.courseName, that.courseName);
        delta.addIfChanged(PropertyType.DESCRIPTION, this.description, that.description);

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

    private Set<String> shallowCourseSet(Set<Course> courses) {
        return courses.stream().map(Course::getUniqueId).collect(Collectors.toSet());
    }

    private static void recordCourseRelationDiff(StructureChangeDelta delta, PropertyType propertyType,
                                                 Collection<Course> thisListing, Collection<Course> thatListing) {

        Collection<Course> removed = new HashSet<>(thisListing);
        Collection<Course> added = new HashSet<>(thatListing);

        for (Course i : thisListing) {
            for (Course j : thatListing) {
                if (i.getUniqueId().equals(j.getUniqueId())) {
                    added.remove(i);
                    removed.remove(j);
                    break;
                }
            }
        }

        added.forEach(z -> delta.addAdded(propertyType, z));
        removed.forEach(z -> delta.addRemoved(propertyType, z));
    }
}
