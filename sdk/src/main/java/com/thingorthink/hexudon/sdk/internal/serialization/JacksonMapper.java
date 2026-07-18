package com.thingorthink.hexudon.sdk.internal.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.thingorthink.hexudon.sdk.exception.HexudonSerializationException;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility wrapper around Jackson ObjectMapper configuration.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *     <li>Provides thread-safe singleton ObjectMapper instance.</li>
 *     <li>Registers custom Coordinate serializer.</li>
 *     <li>Ignores unknown JSON properties from server responses.</li>
 * </ul>
 *
 * <p>Visibility: package-private.</p>
 */
public final class JacksonMapper {

    public static final JacksonMapper INSTANCE =
            new JacksonMapper();

    private final ObjectMapper objectMapper;


    /**
     * Initializes configured Jackson ObjectMapper.
     */
    private JacksonMapper() {

        this.objectMapper =
                new ObjectMapper();

        SimpleModule module =
                new SimpleModule();

        module.addSerializer(
                new CoordinateSerializer()
        );

        module.addDeserializer(
                com.thingorthink.hexudon.sdk.internal.dto.response.MatchConfigResponse.class,
                new MatchConfigDeserializer()
        );

        objectMapper.registerModule(module);

        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );
    }


    /**
     * Serializes Java object into JSON byte array.
     *
     * @param value object to serialize
     * @return JSON byte array
     * @throws HexudonSerializationException
     *         when serialization fails
     */
    public byte[] writeValueAsBytes(
            Object value
    ) {

        Objects.requireNonNull(
                value,
                "Value must not be null"
        );

        try {
            return this.objectMapper
                    .writeValueAsBytes(value);

        } catch (IOException e) {

            throw new HexudonSerializationException(
                    "Failed to serialize object",
                    e
            );
        }
    }


    /**
     * Deserializes JSON byte array into Java object.
     *
     * @param src JSON byte array
     * @param valueType target Java class
     * @param <T> target type
     * @return deserialized Java object
     * @throws HexudonSerializationException
     *         when deserialization fails
     */
    public <T> T readValue(
            byte[] src,
            Class<T> valueType
    ) {

        Objects.requireNonNull(
                src,
                "Source bytes must not be null"
        );

        Objects.requireNonNull(
                valueType,
                "Value type must not be null"
        );

        try {
            return this.objectMapper
                    .readValue(src, valueType);

        } catch (IOException e) {

            throw new HexudonSerializationException(
                    "Failed to deserialize object",
                    e
            );
        }
    }
}
