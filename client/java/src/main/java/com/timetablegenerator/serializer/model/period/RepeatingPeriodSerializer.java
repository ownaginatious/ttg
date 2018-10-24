package com.timetablegenerator.serializer.model.period;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.timetablegenerator.model.period.RepeatingPeriod;
import com.timetablegenerator.model.range.DateRange;
import com.timetablegenerator.model.range.DayTimeRange;
import com.timetablegenerator.serializer.model.Serializer;
import com.timetablegenerator.serializer.model.SerializerContext;

import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RepeatingPeriodSerializer extends PeriodSerializer implements Serializer<RepeatingPeriod> {

    @JsonProperty("term") private String termId = null;
    @JsonProperty("times") private DayTimeRange dayTimeRange = null;
    @JsonProperty("activeDates") private DateRange activeDateRange = null;

    @Override
    public Serializer<RepeatingPeriod> fromInstance(RepeatingPeriod instance){
        super.fromInstance(instance);
        this.termId = instance.getTerm().getUniqueId();
        this.dayTimeRange = instance.getDayTimeRange().orElse(null);
        this.activeDateRange = instance.getActiveDateRange().orElse(null);
        return this;
    }

    @Override
    public RepeatingPeriod toInstance(SerializerContext context){

        RepeatingPeriod instance = RepeatingPeriod.of(context.getTerm(this.termId));
        super.populateInstance(instance);

        Optional.ofNullable(this.dayTimeRange).ifPresent(instance::setDayTimeRange);
        Optional.ofNullable(this.activeDateRange).ifPresent(instance::setActiveDateRange);

        return instance;
    }
}