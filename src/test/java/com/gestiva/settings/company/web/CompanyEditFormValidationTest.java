package com.gestiva.settings.company.web;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyEditFormValidationTest {

    private CompanyEditForm form(String legalName, boolean active) {
        CompanyEditForm form = new CompanyEditForm();
        form.setLegalName(legalName);
        form.setActive(active);
        return form;
    }

    @Test
    void shouldRejectBlankLegalName() {
        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var violations = validator.validate(form("", true));

            assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("legalName")));
        }
    }

    @Test
    void shouldRejectExcessiveLegalNameLength() {
        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            var violations = validator.validate(
                    form("A".repeat(181), true)
            );

            assertTrue(violations.stream().anyMatch(v ->
                    v.getPropertyPath().toString().equals("legalName")));
        }
    }

    @Test
    void shouldAcceptActiveAndInactiveCompanies() {
        try (ValidatorFactory factory =
                     Validation.buildDefaultValidatorFactory()) {

            Validator validator = factory.getValidator();

            assertTrue(
                    validator.validate(
                            form("Società attiva", true)
                    ).isEmpty()
            );

            assertTrue(
                    validator.validate(
                            form("Società inattiva", false)
                    ).isEmpty()
            );
        }
    }
}
