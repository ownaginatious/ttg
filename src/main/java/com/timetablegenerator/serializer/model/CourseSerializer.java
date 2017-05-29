package com.timetablegenerator.serializer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.timetablegenerator.model.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CourseSerializer implements Serializer<Course> {

    @JsonProperty("term") private String termId = null;
    @JsonProperty("department") private String departmentCode = null;
    @JsonProperty("code") private String code = null;
    @JsonProperty("name") private String name = null;
    @JsonProperty("description") private String description = null;
    @JsonProperty("credits") private Double credits = null;

    @JsonDeserialize(contentAs = SectionTypeSerializer.class)
    @JsonProperty("sectionTypes") private Map<String, Serializer<SectionType>> sectionTypes = null;

    @JsonProperty("crossListings") private List<String> crossListings = null;
    @JsonProperty("prerequisites") private List<String> prerequisites = null;
    @JsonProperty("antirequisites") private List<String> antirequisites = null;
    @JsonProperty("corequisites") private List<String> corequisites = null;

    @JsonProperty("notes") private List<String> notes = null;

    @Override
    public Serializer<Course> fromInstance(Course instance) {

        this.termId = instance.getTerm().getUniqueId();
        this.departmentCode = instance.getDepartment().getCode();
        this.code = instance.getCode();
        this.name = instance.getName();
        this.description = instance.getDescription().orElse(null);
        this.credits = instance.getCredits().orElse(null);

        this.sectionTypes = instance.getSectionTypes().stream()
                .collect(Collectors.toMap(
                        x -> x,
                        x -> {
                            Serializer<SectionType> serializer = new SectionTypeSerializer();
                            return serializer.fromInstance(instance.getSectionType(x)
                                    .orElseThrow(IllegalStateException::new));
                        }));

        this.crossListings = instance.getCrossListings().values().stream()
                .map(Course::getUniqueId).collect(Collectors.toList());
        this.prerequisites = instance.getPrerequisites().values().stream()
                .map(Course::getUniqueId).collect(Collectors.toList());
        this.antirequisites = instance.getAntirequisites().values().stream()
                .map(Course::getUniqueId).collect(Collectors.toList());
        this.corequisites = instance.getCorequisites().values().stream()
                .map(Course::getUniqueId).collect(Collectors.toList());

        return this;
    }

    @Override
    public Course toInstance(SerializerContext context) {

        Term term = context.getTerm(this.termId);
        Department department = context.getDepartment(this.departmentCode);

        Course course = Course.of(context.getSchool(), term, department,
                this.code, this.name);

        Optional.ofNullable(this.description).ifPresent(course::setDescription);
        Optional.ofNullable(this.credits).ifPresent(course::setCredits);
        Optional.ofNullable(this.notes).ifPresent(course::addNotes);

        if (this.sectionTypes != null) {

            Map<String, SectionType> sectionTypes = this.sectionTypes.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().toInstance(context)));

            // Validate the section type mapping and term. The Course model does not allow for SectionType
            // instances to be added directly, so the term and code should always be valid. However, when
            // deserializing, we do not know where this data is coming from and should check the term validity
            // anyway to pick up on any discreet bugs that may have occurred outside the system.
            for (Map.Entry<String, SectionType> entry : sectionTypes.entrySet()) {
                if (!entry.getKey().equals(entry.getValue().getCode())) {
                    throw new IllegalStateException(
                            "Serialized course has an invalid section type code -> section type: " +
                                    entry.getKey() + " to " + entry.getValue().getCode());
                }
                entry.getValue().getTerm().assertFallsWithin(term);
            }

            sectionTypes.values().forEach(course::addSections);
        }

        return course;
    }

    public void populateRequisites(Map<String, Course> courses, Course course) {

        if (!course.getTerm().getUniqueId().equals(this.termId) ||
                !course.getCode().equals(this.code) ||
                !course.getDepartment().getCode().equals(this.departmentCode)) {
            throw new IllegalArgumentException("Course \"" + course.getUniqueId()
                    + "\" is unrelated to this serializer instance.");
        }

        Optional.ofNullable(this.notes).ifPresent(course::addNotes);

        Optional.ofNullable(this.crossListings).ifPresent(
                crossListings -> crossListings.forEach(c -> course.addCrossListing(courses.get(c)))
        );
        Optional.ofNullable(this.prerequisites).ifPresent(
                prerequisites -> prerequisites.forEach(c -> course.addPrerequisite(courses.get(c)))
        );
        Optional.ofNullable(this.antirequisites).ifPresent(
                antirequisites -> antirequisites.forEach(c -> course.addAntirequisite(courses.get(c)))
        );
        Optional.ofNullable(this.corequisites).ifPresent(
                corequisites -> corequisites.forEach(c -> course.addCorequesite(courses.get(c)))
        );
    }
}