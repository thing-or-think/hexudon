package com.naprock.hexudon.sdk.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MatchStatusTest {

    @Test
    void shouldGetFromStringCaseInsensitive() {
        assertThat(MatchStatus.fromString("waiting")).isEqualTo(MatchStatus.WAITING);
        assertThat(MatchStatus.fromString("  in_progress  ")).isEqualTo(MatchStatus.PLAYING);
        assertThat(MatchStatus.fromString("FINISHED")).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    void shouldFallbackToWaitingWhenFromStringNullOrUnknown() {
        assertThat(MatchStatus.fromString(null)).isEqualTo(MatchStatus.WAITING);
        assertThat(MatchStatus.fromString("unknown")).isEqualTo(MatchStatus.WAITING);
    }
}
