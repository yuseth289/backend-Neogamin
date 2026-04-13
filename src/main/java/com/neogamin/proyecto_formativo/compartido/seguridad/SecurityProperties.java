package com.neogamin.proyecto_formativo.compartido.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record SecurityProperties(
        String secret,
        long expiracionMinutos
) {
}
