package com.thingorthink.hexudon.sdk.exception;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexudonValidationExceptionTest {

    @Test
    void shouldCreateExceptionWhenMessageOnlyProvided() {
        // Arrange & Act
        HexudonValidationException exception = new HexudonValidationException("invalid request");

        // Assert
        assertThat(exception.getMessage()).isEqualTo("invalid request");
        assertThat(exception.getErrorResponse()).isNotNull();
        assertThat(exception.getErrorResponse().detail()).isEmpty();
    }

    @Test
    void shouldCreateExceptionWhenMessageAndErrorResponseProvided() {
        // Arrange
        HexudonValidationException.ValidationErrorDetail detail = new HexudonValidationException.ValidationErrorDetail(
                List.of("body", "actions"),
                "value is required",
                "missing"
        );
        HexudonValidationException.ErrorResponse errorResponse = new HexudonValidationException.ErrorResponse(List.of(detail));

        // Act
        HexudonValidationException exception = new HexudonValidationException("invalid request", errorResponse);

        // Assert
        assertThat(exception.getMessage()).isEqualTo("invalid request");
        assertThat(exception.getErrorResponse()).isEqualTo(errorResponse);
    }

    @Test
    void shouldThrowWhenErrorResponseIsNull() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HexudonValidationException("invalid request", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("errorResponse must not be null");
    }

    @Test
    void shouldValidateErrorResponseRecord() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HexudonValidationException.ErrorResponse(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("detail must not be null");

        HexudonValidationException.ErrorResponse response = new HexudonValidationException.ErrorResponse(List.of());
        assertThat(response.detail()).isEmpty();
        assertThat(response.toString()).contains("ErrorResponse");
        assertThat(response.hashCode()).isEqualTo(new HexudonValidationException.ErrorResponse(List.of()).hashCode());
        assertThat(response).isEqualTo(new HexudonValidationException.ErrorResponse(List.of()));
    }

    @Test
    void shouldValidateValidationErrorDetailRecord() {
        // Arrange & Act & Assert
        assertThatThrownBy(() -> new HexudonValidationException.ValidationErrorDetail(null, "msg", "type"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new HexudonValidationException.ValidationErrorDetail(List.of(), null, "type"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new HexudonValidationException.ValidationErrorDetail(List.of(), "msg", null))
                .isInstanceOf(NullPointerException.class);

        HexudonValidationException.ValidationErrorDetail detail1 = new HexudonValidationException.ValidationErrorDetail(
                List.of("path"), "msg", "type"
        );
        HexudonValidationException.ValidationErrorDetail detail2 = new HexudonValidationException.ValidationErrorDetail(
                List.of("path"), "msg", "type"
        );

        assertThat(detail1.loc()).containsExactly("path");
        assertThat(detail1.msg()).isEqualTo("msg");
        assertThat(detail1.type()).isEqualTo("type");
        assertThat(detail1).isEqualTo(detail2);
        assertThat(detail1.hashCode()).isEqualTo(detail2.hashCode());
        assertThat(detail1.toString()).contains("loc", "msg", "type");
    }
}
