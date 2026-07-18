package com.thingorthink.hexudon.sdk.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexudonConfigBuilderTest {

    @Test
    void shouldBuildConfigWithExplicitValues() {
        // Arrange
        HttpClientConfig httpConfig = new HttpClientConfig(1000, 2000, 3000);
        RetryConfig retryConfig = new RetryConfig(5, 500, 1.5);

        // Act
        HexudonConfig config = new HexudonConfigBuilder()
                .baseUrl("https://api.hexudon.com")
                .teamId("my_team")
                .token("my_token")
                .practice(true)
                .httpClientConfig(httpConfig)
                .retryConfig(retryConfig)
                .enableLogging(false)
                .build();

        // Assert
        assertThat(config.baseUrl()).isEqualTo("https://api.hexudon.com");
        assertThat(config.teamId()).isEqualTo("my_team");
        assertThat(config.token()).isEqualTo("my_token");
        assertThat(config.practice()).isTrue();
        assertThat(config.httpClientConfig()).isEqualTo(httpConfig);
        assertThat(config.retryConfig()).isEqualTo(retryConfig);
        assertThat(config.enableLogging()).isFalse();
    }

    @Test
    void shouldBuildConfigWithDefaultValuesAndFallbacks() {
        // Act (Default config)
        HexudonConfig config = new HexudonConfigBuilder()
                .teamId("my_team")
                .token("my_token")
                .build();

        // Assert
        String expectedUrl = System.getenv("HEXUDON_BASE_URL");
        if (expectedUrl == null || expectedUrl.isBlank()) {
            expectedUrl = "http://localhost:8080";
        }
        assertThat(config.baseUrl()).isEqualTo(expectedUrl);
        assertThat(config.httpClientConfig()).isEqualTo(HttpClientConfig.defaultConfig());
        assertThat(config.retryConfig()).isEqualTo(RetryConfig.defaultConfig());
        assertThat(config.enableLogging()).isTrue();
        assertThat(config.practice()).isFalse();

        // Act (Blank baseUrl resolved to fallback)
        HexudonConfig config2 = new HexudonConfigBuilder()
                .baseUrl("   ")
                .teamId("my_team")
                .token("my_token")
                .build();

        // Assert
        assertThat(config2.baseUrl()).isEqualTo(expectedUrl);
    }

    @Test
    void shouldThrowWhenBaseUrlInvalid() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .baseUrl("ftp://localhost")
                .teamId("my_team")
                .token("my_token")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl must start with http:// or https://");

        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .baseUrl("invalid-url")
                .teamId("my_team")
                .token("my_token")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseUrl must start with http:// or https://");
    }

    @Test
    void shouldThrowWhenTeamIdBlank() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .teamId("")
                .token("my_token")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teamId must not be blank");

        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .teamId("   ")
                .token("my_token")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teamId must not be blank");

        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .token("my_token")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teamId must not be blank");
    }

    @Test
    void shouldThrowWhenTokenBlank() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .teamId("my_team")
                .token("")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("token must not be blank");

        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .teamId("my_team")
                .token("   ")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("token must not be blank");

        assertThatThrownBy(() -> new HexudonConfigBuilder()
                .teamId("my_team")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("token must not be blank");
    }
}
