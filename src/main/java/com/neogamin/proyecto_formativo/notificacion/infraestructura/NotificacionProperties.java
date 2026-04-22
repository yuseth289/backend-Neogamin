package com.neogamin.proyecto_formativo.notificacion.infraestructura;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.notificacion")
public record NotificacionProperties(
        @NotBlank(message = "Email remitente must be configured via APP_NOTIFICACION_EMAIL_REMITENTE or app.notificacion.email-remitente")
        @Email(message = "Email remitente must be a valid email address")
        String emailRemitente
) {
}
