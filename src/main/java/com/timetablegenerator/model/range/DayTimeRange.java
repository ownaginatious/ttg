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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DayTimeRange implements Comparable<DayTimeRange> {

    private static final String START = "startTime";
    private static final String END = "endTime";
    private static final String DOW = "dayOfWeek";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @SuppressWarnings("WeakerAccess")
    public static class LocalTimeSerializer extends JsonSerializer<LocalTime> {

        @Override
        public void serialize(LocalTime localTime, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(localTime.toString());
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {

        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return LocalTime.parse(p.getText(), TIME_FORMAT);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class DaySerializer extends JsonSerializer<DayOfWeek> {

        @Override
        public void serialize(DayOfWeek day, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(day.getValue());
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class DayDeserializer extends JsonDeserializer<DayOfWeek> {

        @Override
        public DayOfWeek deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return DayOfWeek.of(p.getIntValue());
        }
    }


    @JsonSerialize(using = DayTimeRange.DaySerializer.class)
    @JsonDeserialize(using = DayTimeRange.DayDeserializer.class)
    @JsonProperty(DOW)
    @Getter
    private final DayOfWeek dayOfWeek;


    @JsonSerialize(using = DayTimeRange.LocalTimeSerializer.class)
    @JsonDeserialize(using = DayTimeRange.LocalTimeDeserializer.class)
    @JsonProperty(START)
    @Getter
    private final LocalTime startTime;

    @JsonSerialize(using = DayTimeRange.LocalTimeSerializer.class)
    @JsonDeserialize(using = DayTimeRange.LocalTimeDeserializer.class)
    @JsonProperty(END)
    @Getter
    private final LocalTime endTime;

    @JsonCreator
    public static DayTimeRange of(@JsonProperty(DOW) @NonNull DayOfWeek dow,
                                  @JsonProperty(START) @NonNull LocalTime startTime,
                                  @JsonProperty(END) @NonNull LocalTime endTime) {
        if (endTime.isBefore(startTime)) { // Catch unconverted AM/PM crossings.
            throw new IllegalArgumentException("The day time range start \"" + startTime
                    + "\" is after the end '" + endTime + "'");
        }
        return new DayTimeRange(dow, startTime, endTime);
    }

    @Override
    public int compareTo(@Nonnull DayTimeRange that) {

        if (this.dayOfWeek != that.dayOfWeek) {
            return this.dayOfWeek.compareTo(that.dayOfWeek);
        } else if (!this.startTime.equals(that.startTime)) {
            return this.startTime.compareTo(that.startTime);
        }
        return this.endTime.compareTo(that.endTime);
    }

    @Override
    public String toString() {
        return dayOfWeek.name() + " " +
                this.startTime.format(TIME_FORMAT) + " -> " + this.endTime.format(TIME_FORMAT);
    }
}
