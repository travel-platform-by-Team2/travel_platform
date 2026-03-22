package com.example.travel_platform.trip;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

class TripRequestValidationTest {

    private final Validator validator = validator();

    @Test
    void createPlanInvalidRegion() {
        TripRequest.CreatePlanDTO reqDTO = validRequest();
        reqDTO.setRegion("tokyo");

        assertTrue(validator.validate(reqDTO).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("region")));
    }

    @Test
    void createPlanInvalidWhoWith() {
        TripRequest.CreatePlanDTO reqDTO = validRequest();
        reqDTO.setWhoWith("team");

        assertTrue(validator.validate(reqDTO).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("whoWith")));
    }

    @Test
    void createPlanValidCodes() {
        TripRequest.CreatePlanDTO reqDTO = validRequest();

        assertFalse(validator.validate(reqDTO).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("region")
                        || violation.getPropertyPath().toString().equals("whoWith")));
    }

    private TripRequest.CreatePlanDTO validRequest() {
        TripRequest.CreatePlanDTO reqDTO = new TripRequest.CreatePlanDTO();
        reqDTO.setTitle("봄 여행");
        reqDTO.setWhoWith("friend");
        reqDTO.setRegion("jeju");
        reqDTO.setStartDate(LocalDate.of(2026, 4, 1));
        reqDTO.setEndDate(LocalDate.of(2026, 4, 3));
        return reqDTO;
    }

    private Validator validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }
}
