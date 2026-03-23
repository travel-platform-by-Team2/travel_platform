package com.example.travel_platform._core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumCodeValidator implements ConstraintValidator<ValidEnumCode, String> {

    private Class<? extends EnumCode> enumClass;

    @Override
    public void initialize(ValidEnumCode constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        EnumCode[] enumCodes = enumClass.getEnumConstants();
        if (enumCodes == null) {
            throw new IllegalStateException(enumClass.getName() + " is not an enum type");
        }

        for (EnumCode enumCode : enumCodes) {
            if (enumCode.getCode().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
