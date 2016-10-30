package com.timetablegenerator.model.range;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateRange implements Comparable<DateRange> {

    private static final String START = "startDate";
    private static final String END = "endDate";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @SuppressWarnings("WeakerAccess")
    public static class LocalDateSerializer extends JsonSerializer<LocalDate> {

        @Override
        public void serialize(LocalDate localDate, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(localDate.toString());
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return LocalDate.parse(p.getText(), DATE_FORMAT);
        }
    }

    private final LocalDate startDate;
    private final LocalDate endDate;

    @JsonCreator
    public static DateRange of(@JsonProperty(START) @NonNull LocalDate startDate,
                               @JsonProperty(END) @NonNull LocalDate endDate){
        if (startDate.isAfter(endDate)){
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("The date range start \"" + startDate
                        + "\" is after the end '" + endDate + "'");
            }
        }
        return new DateRange(startDate, endDate);
    }

    @JsonSerialize(using = DateRange.LocalDateSerializer.class)
    @JsonDeserialize(using = DateRange.LocalDateDeserializer.class)
    @JsonProperty(START)
    public LocalDate getStartDate(){
        return this.startDate;
    }

    @JsonSerialize(using = DateRange.LocalDateSerializer.class)
    @JsonDeserialize(using = DateRange.LocalDateDeserializer.class)
    @JsonProperty(END)
    public LocalDate getEndDate(){
        return this.endDate;
    }

    @Override
    public int compareTo(@Nonnull DateRange that) {
        if (!this.startDate.equals(that.startDate)){
            return this.startDate.compareTo(that.startDate);
        }
        return this.endDate.compareTo(that.endDate);
    }

    @Override
    public String toString(){
        return this.startDate.format(DATE_FORMAT) + " -> " + this.endDate.format(DATE_FORMAT);
    }
}
