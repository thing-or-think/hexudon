package com.thingorthink.hexudon.sdk.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexudonConfigTest {

    @Test
    void shouldCreateHexudonConfigWhenValuesValid() {
        // Arrange
        String baseUrl = "http://localhost:8080";
        String teamId = "team123";
        String token = "secret_token";
        HttpClientConfig httpConfig = HttpClientConfig.defaultConfig();
        RetryConfig retryConfig = RetryConfig.defaultConfig();

        // Act
        HexudonConfig config = new HexudonConfig(
                baseUrl, teamId, token, true, httpConfig, retryConfig, true
        );

        // Assert
        assertThat(config.baseUrl()).isEqualTo(baseUrl);
        assertThat(config.teamId()).isEqualTo(teamId);
        assertThat(config.token()).isEqualTo(token);
        assertThat(config.practice()).isTrue();
        assertThat(config.httpClientConfig()).isEqualTo(httpConfig);
        assertThat(config.retryConfig()).isEqualTo(retryConfig);
        assertThat(config.enableLogging()).isTrue();
    }

    @Test
    void shouldThrowWhenRequiredArgumentsAreNull() {
        HttpClientConfig httpConfig = HttpClientConfig.defaultConfig();
        RetryConfig retryConfig = RetryConfig.defaultConfig();

        assertThatThrownBy(() -> new HexudonConfig(null, "teamId", "token", false, httpConfig, retryConfig, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("baseUrl must not be null");

        assertThatThrownBy(() -> new HexudonConfig("http://localhost", null, "token", false, httpConfig, retryConfig, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("teamId must not be null");

        assertThatThrownBy(() -> new HexudonConfig("http://localhost", "teamId", null, false, httpConfig, retryConfig, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("token must not be null");

        assertThatThrownBy(() -> new HexudonConfig("http://localhost", "teamId", "token", false, null, retryConfig, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("httpClientConfig must not be null");

        assertThatThrownBy(() -> new HexudonConfig("http://localhost", "teamId", "token", false, httpConfig, null, false))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("retryConfig must not be null");
    }

    @Test
    void shouldMaskTokenInToString() {
        // Arrange
        HexudonConfig config = new HexudonConfig(
                "http://localhost", "teamId", "my_secret_token", false,
                HttpClientConfig.defaultConfig(), RetryConfig.defaultConfig(), false
        );

        // Act
        String str = config.toString();

        // Assert
        assertThat(str).contains("[PROTECTED]");
        assertThat(str).doesNotContain("my_secret_token");
    }

    @Test
    void shouldSupportStaticBuilder() {
        // Act
        HexudonConfigBuilder builder = HexudonConfig.builder();

        // Assert
        assertThat(builder).isNotNull();
    }
}
