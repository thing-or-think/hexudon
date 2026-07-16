package com.naprock.hexudon.sdk.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.naprock.hexudon.sdk.model.Coordinate;

import java.io.IOException;

/**
 * Custom Jackson serializer for Coordinate.
 *
 * <p>
 * Serializes Coordinate as a single integer position value
 * instead of a complex JSON object.
 *
 * <p>
 * Example:
 * <pre>
 * Coordinate(15, 3)
 *
 * Default JSON:
 * {
 *   "q": 15,
 *   "r": 3
 * }
 *
 * Serialized JSON:
 * 123
 * </pre>
 */
final class CoordinateSerializer extends StdSerializer<Coordinate> {

    /**
     * Creates Coordinate serializer.
     */
    CoordinateSerializer() {
        super(Coordinate.class);
    }

    /**
     * Writes Coordinate value as linear position integer.
     *
     * @param value coordinate value
     * @param gen JSON generator
     * @param provider serializer provider
     * @throws IOException when JSON writing fails
     */
    @Override
    public void serialize(
            Coordinate value,
            JsonGenerator gen,
            SerializerProvider provider
    ) throws IOException {

        if (value == null) {
            throw new IllegalArgumentException(
                    "Coordinate value must not be null"
            );
        }

        gen.writeNumber(value.pos());
    }
}