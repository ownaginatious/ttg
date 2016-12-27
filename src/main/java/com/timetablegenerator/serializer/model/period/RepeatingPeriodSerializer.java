package com.timetablegenerator.serializer.model.period;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.model.range.DateRange;
import com.timetablegenerator.model.range.DayTimeRange;
import com.timetablegenerator.serializer.model.Serializer;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepeatingPeriodSerializer extends PeriodSerializer implements Serializer<RepeatingPeriod> {

    @JsonProperty("term") private String termId = null;
    @JsonProperty("times") private DayTimeRange dayTimeRange = null;
    @JsonProperty("activeDates") private DateRange activeDateRange = null;

    @Override
    public void fromInstance(RepeatingPeriod instance){
        super.fromInstance(instance);
        this.termId = instance.getTerm().getUniqueId();
        this.dayTimeRange = instance.getDayTimeRange().orElse(null);
        this.activeDateRange = instance.getActiveDateRange().orElse(null);
    }

    @Override
    public RepeatingPeriod toInstance(School school, Map<String, Term> terms){

        RepeatingPeriod instance = RepeatingPeriod.of(terms.get(this.termId));
        super.populateInstance(instance);

        if (this.dayTimeRange != null){
            instance.setDayTimeRange(this.dayTimeRange);
        }
        if (this.activeDateRange != null){
            instance.setActiveDateRange(this.activeDateRange);
        }
        return instance;
    }
}