package com.neogamin.proyecto_formativo.notificacion.infraestructura;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class NotificacionPropertiesTest {

    @Test
    void shouldRequireValidSenderEmail() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(new NotificacionProperties(false, "correo-invalido"));

        assertThat(violations)
                .extracting(violation -> violation.getMessage())
                .contains("Email remitente must be a valid email address");
    }

    @Test
    void shouldRequireMailCredentials() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(new CorreoSmtpProperties("", ""));

        assertThat(violations)
                .extracting(violation -> violation.getMessage())
                .contains(
                        "Mail username must be configured via MAIL_USERNAME or spring.mail.username",
                        "Mail password must be configured via MAIL_PASSWORD or spring.mail.password"
                );
    }
}
