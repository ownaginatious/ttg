package com.timetablegenerator.serializer.model.period;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetablegenerator.model.period.Period;

import java.util.List;
import java.util.Optional;
import java.util.Set;

class PeriodSerializer {

    @JsonProperty("supervisors") private Set<String> supervisors = null;
    @JsonProperty("notes") private List<String> notes = null;
    @JsonProperty("campus") private String campus = null;
    @JsonProperty("room") private String room = null;
    @JsonProperty("online") private Boolean online = null;

    void fromInstance(Period instance){

        this.supervisors = instance.getSupervisors();
        this.notes = instance.getNotes();

        this.campus = instance.getCampus().orElse(null);
        this.room = instance.getRoom().orElse(null);
        this.online = instance.isOnline().orElse(null);
    }

    void populateInstance(Period period){

        Optional.ofNullable(this.supervisors).ifPresent(period::addSupervisors);
        Optional.ofNullable(this.notes).ifPresent(period::addNotes);

        Optional.ofNullable(this.campus).ifPresent(period::setCampus);
        Optional.ofNullable(this.room).ifPresent(period::setRoom);
        Optional.ofNullable(this.online).ifPresent(period::setOnline);
    }
}