package com.naprock.hexudon.sdk.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryConfigTest {

    @Test
    void shouldCreateRetryConfigWhenValuesValid() {
        // Arrange & Act
        RetryConfig config = new RetryConfig(5, 500, 1.5);

        // Assert
        assertThat(config.maxRetries()).isEqualTo(5);
        assertThat(config.retryDelayMs()).isEqualTo(500);
        assertThat(config.retryMultiplier()).isEqualTo(1.5);
    }

    @Test
    void shouldThrowWhenMaxRetriesNegative() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new RetryConfig(-1, 500, 1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRetries must be greater than or equal to 0");
    }

    @Test
    void shouldThrowWhenRetryDelayMsNegative() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new RetryConfig(5, -1, 1.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retryDelayMs must be greater than or equal to 0");
    }

    @Test
    void shouldThrowWhenRetryMultiplierNegative() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new RetryConfig(5, 500, -0.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("retryMultiplier must be greater than or equal to 0");
    }

    @Test
    void shouldReturnDefaultConfig() {
        // Arrange & Act
        RetryConfig defaultConfig = RetryConfig.defaultConfig();

        // Assert
        assertThat(defaultConfig).isNotNull();
        assertThat(defaultConfig.maxRetries()).isEqualTo(3);
        assertThat(defaultConfig.retryDelayMs()).isEqualTo(1000L);
        assertThat(defaultConfig.retryMultiplier()).isEqualTo(2.0);
    }

    @Test
    void shouldSupportRecordEqualsHashCodeToString() {
        // Arrange
        RetryConfig config1 = new RetryConfig(3, 1000, 2.0);
        RetryConfig config2 = new RetryConfig(3, 1000, 2.0);
        RetryConfig config3 = new RetryConfig(3, 1000, 2.5);

        // Assert
        assertThat(config1).isEqualTo(config2);
        assertThat(config1).isNotEqualTo(config3);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        assertThat(config1.toString()).contains("maxRetries", "retryDelayMs", "retryMultiplier");
    }
}
