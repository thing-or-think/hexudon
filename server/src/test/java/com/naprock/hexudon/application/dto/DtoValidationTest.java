package com.naprock.hexudon.application.dto;

import com.naprock.hexudon.application.dto.match.SubmitActionRequest;
import com.naprock.hexudon.application.dto.team.TeamRegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testTeamRegisterRequest_valid() {
        TeamRegisterRequest request = new TeamRegisterRequest(List.of(0, 1));
        Set<ConstraintViolation<TeamRegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testTeamRegisterRequest_invalidTypes() {
        TeamRegisterRequest requestEmpty = new TeamRegisterRequest(Collections.emptyList());
        Set<ConstraintViolation<TeamRegisterRequest>> violationsEmpty = validator.validate(requestEmpty);
        assertFalse(violationsEmpty.isEmpty());

        TeamRegisterRequest requestInvalidValues = new TeamRegisterRequest(List.of(-1, 2));
        Set<ConstraintViolation<TeamRegisterRequest>> violationsInvalid = validator.validate(requestInvalidValues);
        assertEquals(2, violationsInvalid.size());
    }

    @Test
    void testSubmitActionRequest_valid() {
        SubmitActionRequest request = new SubmitActionRequest(5, List.of(List.of(0, -6, 6)));
        Set<ConstraintViolation<SubmitActionRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testSubmitActionRequest_invalidDay() {
        SubmitActionRequest request = new SubmitActionRequest(-1, List.of(List.of(0)));
        Set<ConstraintViolation<SubmitActionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Day must be greater than or equal to 0")));
    }

    @Test
    void testSubmitActionRequest_invalidActionValues() {
        SubmitActionRequest request = new SubmitActionRequest(1, List.of(List.of(-7, 7)));
        Set<ConstraintViolation<SubmitActionRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }
}
