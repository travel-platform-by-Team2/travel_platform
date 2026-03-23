package com.example.travel_platform.board;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

class BoardRequestValidationTest {

    private final Validator validator = validator();

    @Test
    void createInvalidCategory() {
        BoardRequest.CreateDTO reqDTO = validCreateRequest();
        reqDTO.setCategory("notice");

        assertTrue(validator.validate(reqDTO).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("category")));
    }

    @Test
    void updateInvalidCategory() {
        BoardRequest.UpdateDTO reqDTO = validUpdateRequest();
        reqDTO.setCategory("notice");

        assertTrue(validator.validate(reqDTO).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("category")));
    }

    @Test
    void validCategory() {
        BoardRequest.CreateDTO reqDTO = validCreateRequest();

        assertFalse(validator.validate(reqDTO).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("category")));
    }

    private BoardRequest.CreateDTO validCreateRequest() {
        BoardRequest.CreateDTO reqDTO = new BoardRequest.CreateDTO();
        reqDTO.setCategory("tips");
        reqDTO.setTitle("제목");
        reqDTO.setContent("내용");
        return reqDTO;
    }

    private BoardRequest.UpdateDTO validUpdateRequest() {
        BoardRequest.UpdateDTO reqDTO = new BoardRequest.UpdateDTO();
        reqDTO.setCategory("review");
        reqDTO.setTitle("제목");
        reqDTO.setContent("내용");
        return reqDTO;
    }

    private Validator validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return validator;
    }
}
