package com.timetablegenerator.serializer.model.period;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetablegenerator.model.period.OneTimePeriod;
import com.timetablegenerator.model.range.DateTimeRange;
import com.timetablegenerator.serializer.model.Serializer;
import com.timetablegenerator.serializer.model.SerializerContext;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OneTimePeriodSerializer extends PeriodSerializer implements Serializer<OneTimePeriod> {

    @JsonProperty("term") private String termId = null;
    @JsonProperty("times") private DateTimeRange dateTimeRange = null;


    @Override
    public Serializer<OneTimePeriod> fromInstance(OneTimePeriod instance){
        super.fromInstance(instance);
        this.termId = instance.getTerm().getUniqueId();
        this.dateTimeRange = instance.getDateTimeRange().orElse(null);
        return this;
    }


    @Override
    public OneTimePeriod toInstance(SerializerContext context){

        OneTimePeriod instance = OneTimePeriod.of(context.getTerm(this.termId));
        super.populateInstance(instance);

        Optional.ofNullable(this.dateTimeRange).ifPresent(instance::setDateTimeRange);

        return instance;
    }
}