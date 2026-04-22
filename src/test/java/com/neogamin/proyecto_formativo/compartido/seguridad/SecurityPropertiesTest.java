package com.neogamin.proyecto_formativo.compartido.seguridad;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class SecurityPropertiesTest {

    @Test
    void shouldRequireJwtSecret() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(new SecurityProperties("", 120));

        assertThat(violations)
                .extracting(violation -> violation.getMessage())
                .contains("JWT secret must be configured via JWT_SECRET or app.security.jwt.secret");
    }

    @Test
    void shouldAcceptConfiguredJwtSecret() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(new SecurityProperties("0123456789abcdef0123456789abcdef", 120));

        assertThat(violations).isEmpty();
    }
}
