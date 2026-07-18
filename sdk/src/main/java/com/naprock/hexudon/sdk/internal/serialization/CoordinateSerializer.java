package com.naprock.hexudon.sdk.internal.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.naprock.hexudon.sdk.model.Coordinate;

import java.io.IOException;

/**
 * Custom Jackson serializer for Coordinate.
 *
 * <p>This serializer converts a Coordinate object into a single
 * linear position index when writing JSON.</p>
 *
 * <p>Visibility: package-private.</p>
 */
final class CoordinateSerializer
        extends StdSerializer<Coordinate> {

    /**
     * Creates a serializer for Coordinate type.
     */
    public CoordinateSerializer() {
        super(Coordinate.class);
    }

    /**
     * Serializes Coordinate as a raw integer position.
     *
     * @param value coordinate value
     * @param gen JSON generator
     * @param provider serializer provider
     * @throws IOException when JSON stream writing fails
     */
    @Override
    public void serialize(
            Coordinate value,
            JsonGenerator gen,
            SerializerProvider provider
    ) throws IOException {

        gen.writeNumber(value.pos());
    }
}
