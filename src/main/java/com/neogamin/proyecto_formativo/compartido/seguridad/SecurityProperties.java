package com.neogamin.proyecto_formativo.compartido.seguridad;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record SecurityProperties(
        @NotBlank(message = "JWT secret must be configured via JWT_SECRET or app.security.jwt.secret")
        @Size(min = 32, message = "JWT secret must be at least 32 characters long")
        String secret,
        @Min(value = 1, message = "JWT expiration minutes must be greater than zero")
        long expiracionMinutos
) {
}
