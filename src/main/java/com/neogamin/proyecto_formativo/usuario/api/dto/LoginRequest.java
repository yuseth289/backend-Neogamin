package com.neogamin.proyecto_formativo.usuario.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Correo del usuario", example = "admin@neogamin.com")
        @Email @NotBlank String email,
        @Schema(description = "Contrasena del usuario", example = "Admin123*")
        @NotBlank String password
) {
}
