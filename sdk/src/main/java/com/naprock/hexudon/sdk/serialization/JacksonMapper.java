package com.naprock.hexudon.sdk.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.naprock.hexudon.sdk.exception.HexudonException;
import com.naprock.hexudon.sdk.model.Coordinate;

/**
 * Internal JSON serialization and deserialization utility.
 *
 * <p>
 * This class manages Jackson ObjectMapper configuration used internally
 * by Hexudon SDK.
 *
 * <p>
 * Thread-safe because Jackson ObjectMapper is thread-safe after configuration.
 */
public final class JacksonMapper {

    private final ObjectMapper objectMapper;

    /**
     * Creates and configures Jackson ObjectMapper.
     *
     * <p>
     * Configuration:
     * <ul>
     *     <li>Register Coordinate custom serializer.</li>
     *     <li>Ignore unknown JSON properties for forward compatibility.</li>
     *     <li>Exclude null fields from JSON payload.</li>
     * </ul>
     */
    public JacksonMapper() {
        this.objectMapper = new ObjectMapper();

        configure();
    }

    private void configure() {

        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        objectMapper.setSerializationInclusion(
                JsonInclude.Include.NON_NULL
        );

        SimpleModule module = new SimpleModule();

        module.addSerializer(
                Coordinate.class,
                new CoordinateSerializer()
        );

        objectMapper.registerModule(module);
    }

    /**
     * Returns configured ObjectMapper instance.
     *
     * @return configured ObjectMapper
     */
    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Serializes Java object into JSON bytes.
     *
     * @param value object to serialize
     * @return JSON byte array
     * @throws HexudonException when serialization fails
     */
    public byte[] writeValueAsBytes(Object value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "value must not be null"
            );
        }

        try {
            return objectMapper.writeValueAsBytes(value);

        } catch (JsonProcessingException e) {
            throw new HexudonException(
                    "Failed to serialize object to JSON",
                    e
            );
        }
    }

    /**
     * Deserializes JSON bytes into Java object.
     *
     * @param src JSON bytes
     * @param valueType target class
     * @param <T> target type
     * @return deserialized object
     * @throws HexudonException when parsing fails
     */
    public <T> T readValue(byte[] src, Class<T> valueType) {

        if (src == null) {
            throw new IllegalArgumentException(
                    "src must not be null"
            );
        }

        if (valueType == null) {
            throw new IllegalArgumentException(
                    "valueType must not be null"
            );
        }

        try {
            return objectMapper.readValue(src, valueType);

        } catch (Exception e) {
            throw new HexudonException(
                    "Failed to deserialize JSON",
                    e
            );
        }
    }
}