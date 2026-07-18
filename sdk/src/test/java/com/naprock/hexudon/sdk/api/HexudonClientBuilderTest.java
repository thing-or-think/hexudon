package com.naprock.hexudon.sdk.api;

import com.naprock.hexudon.sdk.config.HexudonConfig;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

class HexudonClientBuilderTest {

    @Test
    void shouldBuildClientUsingExplicitConfig() {
        // Arrange
        HexudonConfig config = HexudonConfig.builder()
                .baseUrl("http://localhost")
                .teamId("team1")
                .token("token")
                .build();

        OkHttpClient mockClient = mock(OkHttpClient.class);

        try (MockedConstruction<OkHttpClient.Builder> builderConstruction = Mockito.mockConstruction(
                OkHttpClient.Builder.class,
                Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SELF),
                (mock, context) -> {
                    lenient().when(mock.build()).thenReturn(mockClient);
                }
        )) {
            // Act
            HexudonClient client = new HexudonClientBuilder()
                    .config(config)
                    .build();

            // Assert
            assertThat(client).isNotNull();
            assertThat(client.getClass().getSimpleName()).isEqualTo("DefaultHexudonClient");
        }
    }

    @Test
    void shouldBuildClientUsingIndividualSetters() {
        // Arrange
        OkHttpClient mockClient = mock(OkHttpClient.class);

        try (MockedConstruction<OkHttpClient.Builder> builderConstruction = Mockito.mockConstruction(
                OkHttpClient.Builder.class,
                Mockito.withSettings().defaultAnswer(Mockito.RETURNS_SELF),
                (mock, context) -> {
                    lenient().when(mock.build()).thenReturn(mockClient);
                }
        )) {
            // Act
            HexudonClient client = new HexudonClientBuilder()
                    .baseUrl("http://localhost")
                    .teamId("team1")
                    .token("token")
                    .practice(true)
                    .enableLogging(false)
                    .build();

            // Assert
            assertThat(client).isNotNull();
            assertThat(client.getClass().getSimpleName()).isEqualTo("DefaultHexudonClient");
        }
    }

    @Test
    void shouldThrowWhenConfigIsNull() {
        assertThatThrownBy(() -> new HexudonClientBuilder().config(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("config must not be null");
    }
}
