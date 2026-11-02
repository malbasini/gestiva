package com.gestiva.settings.company.web;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyCreateFormValidationTest {

    private CompanyCreateForm form(String code, String legalName) {
        CompanyCreateForm form = new CompanyCreateForm();
        form.setCode(code);
        form.setLegalName(legalName);
        return form;
    }

    @Test
    void shouldRejectBlankFields() {
        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var violations = validator.validate(form("", ""));

            assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("code")));

            assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("legalName")));
        }
    }

    @Test
    void shouldRejectExcessiveLengths() {
        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var violations = validator.validate(
                    form("A".repeat(31), "B".repeat(181))
            );

            assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("code")));

            assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("legalName")));
        }
    }

    @Test
    void shouldAcceptValidForm() {
        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var violations = validator.validate(
                    form("TEST01", "Società di prova")
            );

            assertTrue(violations.isEmpty());
        }
    }
}
