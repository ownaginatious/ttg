package com.timetablegenerator.serializer.model;

/**
 * An intermediary serialization class for converting a model instance into
 * some serialization format (e.g. JSON).
 *
 * @param <T> A timetable generator model.
 */
public interface Serializer<T> {

    /**
     * Converts a populated serializer into an instance.
     *
     * @param context Context that may be needed for reconstructing the
     *                instance.
     * @return A new instance constructed from the serializer.
     */
    T toInstance(SerializerContext context);

    /**
     * Populates a serializer from an instance.
     *
     * @param instance The instance to populate the serializer from.
     * @return The serializer instance.
     */
    Serializer<T> fromInstance(T instance);
}
