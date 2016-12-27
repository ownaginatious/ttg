package com.timetablegenerator.serializer.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.time.DayOfWeek;

@UtilityClass
public class DaySerializer {

    public class Serializer extends JsonSerializer<DayOfWeek> {
        @Override
        public void serialize(DayOfWeek day, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeNumber(day.getValue());
        }
    }

    public class Deserializer extends JsonDeserializer<DayOfWeek> {
        @Override
        public DayOfWeek deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return DayOfWeek.of(p.getIntValue());
        }
    }
}