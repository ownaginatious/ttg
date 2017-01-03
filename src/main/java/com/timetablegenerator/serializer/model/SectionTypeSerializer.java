package com.timetablegenerator.serializer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.timetablegenerator.model.Section;
import com.timetablegenerator.model.SectionType;

import java.util.Map;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SectionTypeSerializer implements Serializer<SectionType> {

    @JsonProperty("code") private String code = null;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @JsonProperty("name") private String name = null;

    @JsonDeserialize(contentAs = SectionSerializer.class)
    @JsonProperty("sections")
    private Map<String, Serializer<Section>> sections = null;

    @Override
    public Serializer<SectionType> fromInstance(SectionType instance) {

        this.code = instance.getCode();
        this.name = instance.getName();

        this.sections = instance.getSections().stream()
                .collect(Collectors.toMap(Section::getId, section -> {
                    SectionSerializer serializer = new SectionSerializer();
                    return serializer.fromInstance(section);
                }));

        return this;
    }

    @Override
    public SectionType toInstance(SerializerContext context) {

        SectionType sectionType = SectionType.of(context.getSchool(), this.code);

        if (this.sections != null) {

            Map<String, Section> sections = this.sections.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().toInstance(context)));

            // Validate the section mapping.
            for (Map.Entry<String, Section> entry : sections.entrySet()) {
                if (!entry.getKey().equals(entry.getValue().getId())) {
                    throw new IllegalStateException(
                            "Serialized section type has an invalid section ID -> section mapping: " +
                                    entry.getKey() + " to " + entry.getValue().getId());
                }
            }

            sections.values().forEach(sectionType::addSection);
        }

        return sectionType;
    }
}