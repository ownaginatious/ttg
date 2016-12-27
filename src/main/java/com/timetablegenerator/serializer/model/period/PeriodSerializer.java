package com.timetablegenerator.serializer.model.period;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetablegenerator.model.period.Period;

import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
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

        period.addSupervisors(this.supervisors);
        period.addNotes(this.notes);

        if (this.campus != null) {
            period.setCampus(this.campus);
        }

        if (this.room != null) {
            period.setCampus(this.room);
        }

        if (this.online != null) {
            period.setOnline(this.online);
        }
    }
}