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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeRange implements Comparable<DateTimeRange> {

    private static final String START = "startDateTime";
    private static final String END = "endDateTime";

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @SuppressWarnings("WeakerAccess")
    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeString(localDateTime.toString());
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return LocalDateTime.parse(p.getText(), DATETIME_FORMAT);
        }
    }

    @JsonSerialize(using = DateTimeRange.LocalDateTimeSerializer.class)
    @JsonDeserialize(using = DateTimeRange.LocalDateTimeDeserializer.class)
    @JsonProperty(START)
    @Getter
    private final LocalDateTime startDateTime;

    @JsonSerialize(using = DateTimeRange.LocalDateTimeSerializer.class)
    @JsonDeserialize(using = DateTimeRange.LocalDateTimeDeserializer.class)
    @JsonProperty(END)
    @Getter
    private final LocalDateTime endDateTime;

    @JsonCreator
    public static DateTimeRange of(@JsonProperty(START) @NonNull LocalDateTime startDateTime,
                                   @JsonProperty(END) @NonNull LocalDateTime endDateTime) {
        if (startDateTime.isAfter(endDateTime)){
            if (startDateTime.isAfter(endDateTime)) {
                throw new IllegalArgumentException("The datetime range start '" + startDateTime
                        + "' is after the end '" + endDateTime + "'");
            }
        }
        return new DateTimeRange(startDateTime, endDateTime);
    }

    @Override
    public int compareTo(@Nonnull DateTimeRange that) {
        if (!this.startDateTime.equals(that.startDateTime)) {
            return this.startDateTime.compareTo(that.startDateTime);
        }
        return this.endDateTime.compareTo(that.endDateTime);
    }

    @Override
    public String toString(){
        return this.startDateTime.format(DATETIME_FORMAT) + " -> " + this.endDateTime.format(DATETIME_FORMAT);
    }
}
