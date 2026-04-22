package com.neogamin.proyecto_formativo.notificacion.infraestructura;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.mail")
public record CorreoSmtpProperties(
        @NotBlank(message = "Mail username must be configured via MAIL_USERNAME or spring.mail.username")
        String username,
        @NotBlank(message = "Mail password must be configured via MAIL_PASSWORD or spring.mail.password")
        String password
) {
}
