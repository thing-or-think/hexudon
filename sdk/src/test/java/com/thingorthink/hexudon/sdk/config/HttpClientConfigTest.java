package com.thingorthink.hexudon.sdk.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpClientConfigTest {

    @Test
    void shouldCreateHttpClientConfigWhenTimeoutsValid() {
        // Arrange & Act
        HttpClientConfig config = new HttpClientConfig(1000, 2000, 3000);

        // Assert
        assertThat(config.connectTimeoutMs()).isEqualTo(1000);
        assertThat(config.readTimeoutMs()).isEqualTo(2000);
        assertThat(config.writeTimeoutMs()).isEqualTo(3000);
    }

    @Test
    void shouldThrowWhenConnectTimeoutMsNegative() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HttpClientConfig(-1, 2000, 3000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("connectTimeoutMs must be greater than or equal to 0");
    }

    @Test
    void shouldThrowWhenReadTimeoutMsNegative() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HttpClientConfig(1000, -1, 3000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("readTimeoutMs must be greater than or equal to 0");
    }

    @Test
    void shouldThrowWhenWriteTimeoutMsNegative() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HttpClientConfig(1000, 2000, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("writeTimeoutMs must be greater than or equal to 0");
    }

    @Test
    void shouldReturnDefaultConfig() {
        // Arrange & Act
        HttpClientConfig defaultConfig = HttpClientConfig.defaultConfig();

        // Assert
        assertThat(defaultConfig).isNotNull();
        assertThat(defaultConfig.connectTimeoutMs()).isEqualTo(5000);
        assertThat(defaultConfig.readTimeoutMs()).isEqualTo(10000);
        assertThat(defaultConfig.writeTimeoutMs()).isEqualTo(10000);
    }

    @Test
    void shouldSupportRecordEqualsHashCodeToString() {
        // Arrange
        HttpClientConfig config1 = new HttpClientConfig(1000, 2000, 3000);
        HttpClientConfig config2 = new HttpClientConfig(1000, 2000, 3000);
        HttpClientConfig config3 = new HttpClientConfig(1000, 2000, 4000);

        // Assert
        assertThat(config1).isEqualTo(config2);
        assertThat(config1).isNotEqualTo(config3);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        assertThat(config1.toString()).contains("connectTimeoutMs", "readTimeoutMs", "writeTimeoutMs");
    }
}
