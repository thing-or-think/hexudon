package com.naprock.hexudon.sdk.internal.serialization;

import com.naprock.hexudon.sdk.exception.HexudonSerializationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonMapperTest {

    static record DummyRecord(String name, int age) {}

    @Test
    void shouldSerializeAndDeserializeSuccessfully() {
        // Arrange
        DummyRecord record = new DummyRecord("Alice", 30);

        // Act
        byte[] bytes = JacksonMapper.INSTANCE.writeValueAsBytes(record);
        DummyRecord deserialized = JacksonMapper.INSTANCE.readValue(bytes, DummyRecord.class);

        // Assert
        assertThat(deserialized).isEqualTo(record);
    }

    @Test
    void shouldIgnoreUnknownProperties() {
        // Arrange
        String json = "{\"name\":\"Bob\",\"age\":25,\"unknown_field\":\"ignored\"}";

        // Act
        DummyRecord deserialized = JacksonMapper.INSTANCE.readValue(json.getBytes(), DummyRecord.class);

        // Assert
        assertThat(deserialized.name()).isEqualTo("Bob");
        assertThat(deserialized.age()).isEqualTo(25);
    }

    @Test
    void shouldThrowSerializationExceptionWhenWriteFails() {
        // Arrange
        Object selfReferencingObject = new Object() {
            public Object getSelf() { return this; }
        };

        // Act & Assert
        assertThatThrownBy(() -> JacksonMapper.INSTANCE.writeValueAsBytes(selfReferencingObject))
                .isInstanceOf(HexudonSerializationException.class)
                .hasMessageContaining("Failed to serialize object");
    }

    @Test
    void shouldThrowSerializationExceptionWhenReadFails() {
        // Arrange
        byte[] invalidJson = "invalid-json".getBytes();

        // Act & Assert
        assertThatThrownBy(() -> JacksonMapper.INSTANCE.readValue(invalidJson, DummyRecord.class))
                .isInstanceOf(HexudonSerializationException.class)
                .hasMessageContaining("Failed to deserialize object");
    }

    @Test
    void shouldThrowWhenArgumentsAreNull() {
        assertThatThrownBy(() -> JacksonMapper.INSTANCE.writeValueAsBytes(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> JacksonMapper.INSTANCE.readValue(null, DummyRecord.class))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> JacksonMapper.INSTANCE.readValue(new byte[0], null))
                .isInstanceOf(NullPointerException.class);
    }
}
