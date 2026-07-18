package com.naprock.hexudon.sdk.internal.client;

import com.naprock.hexudon.sdk.config.HexudonConfig;
import com.naprock.hexudon.sdk.internal.http.HttpExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultHexudonClientTest {

    @Mock
    private HttpExecutor httpExecutor;

    @Test
    void shouldCreateClientAndExposeApis() throws Exception {
        // Arrange
        HexudonConfig config = HexudonConfig.builder()
                .baseUrl("http://localhost")
                .teamId("team1")
                .token("token")
                .build();

        // Act
        DefaultHexudonClient client = new DefaultHexudonClient(config, httpExecutor);

        // Assert
        assertThat(client.game()).isInstanceOf(DefaultGameApi.class);
        assertThat(client.practice()).isInstanceOf(DefaultPracticeApi.class);

        // Act
        client.close();

        // Assert
        verify(httpExecutor).close();
    }

    @Test
    void shouldThrowWhenConstructorParametersNull() {
        HexudonConfig config = HexudonConfig.builder()
                .baseUrl("http://localhost")
                .teamId("team1")
                .token("token")
                .build();

        assertThatThrownBy(() -> new DefaultHexudonClient(null, httpExecutor))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new DefaultHexudonClient(config, null))
                .isInstanceOf(NullPointerException.class);
    }
}
