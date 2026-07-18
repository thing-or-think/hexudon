package com.thingorthink.hexudon.sdk.internal.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.thingorthink.hexudon.sdk.model.Coordinate;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CoordinateSerializerTest {

    @Test
    void shouldSerializeCoordinateToRawInteger() throws IOException {
        // Arrange
        Coordinate coordinate = new Coordinate(42, 2, 3);
        JsonGenerator mockGen = mock(JsonGenerator.class);
        SerializerProvider mockProvider = mock(SerializerProvider.class);
        CoordinateSerializer serializer = new CoordinateSerializer();

        // Act
        serializer.serialize(coordinate, mockGen, mockProvider);

        // Assert
        verify(mockGen).writeNumber(42);
    }

    @Test
    void shouldIntegrationSerializeWithJacksonMapper() {
        // Arrange
        Coordinate coordinate = new Coordinate(42, 2, 3);

        // Act
        byte[] bytes = JacksonMapper.INSTANCE.writeValueAsBytes(coordinate);
        String json = new String(bytes);

        // Assert
        assertThat(json).isEqualTo("42");
    }
}
