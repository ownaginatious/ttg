package com.timetablegenerator.serializer.model.period;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetablegenerator.model.School;
import com.timetablegenerator.model.Term;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.range.DateTimeRange;
import com.timetablegenerator.serializer.model.Serializer;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OneTimePeriodSerializer extends PeriodSerializer implements Serializer<OneTimePeriod> {

    @JsonProperty("term") private String termId = null;
    @JsonProperty("times") private DateTimeRange dateTimeRange = null;

    @Override
    public void fromInstance(OneTimePeriod instance){
        super.fromInstance(instance);
        this.termId = instance.getTerm().getUniqueId();
        this.dateTimeRange = instance.getDateTimeRange().orElse(null);
    }

    @Override
    public OneTimePeriod toInstance(School school, Map<String, Term> terms){

        OneTimePeriod instance = OneTimePeriod.of(terms.get(this.termId));
        super.populateInstance(instance);

        if (this.dateTimeRange != null){
            instance.setDateTimeRange(this.dateTimeRange);
        }
        return instance;
    }
}