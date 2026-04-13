package com.neogamin.proyecto_formativo.usuario.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
        @Schema(description = "Nombre completo del usuario", example = "Carlos Mendoza")
        @NotBlank @Size(max = 120) String nombre,
        @Schema(description = "Correo electronico unico del usuario", example = "carlos@gmail.com")
        @Email @NotBlank @Size(max = 190) String email,
        @Schema(description = "Contrasena en texto plano que sera hasheada con BCrypt", example = "Carlos123*")
        @NotBlank @Size(min = 8, max = 100) String password,
        @Schema(description = "Telefono de contacto del usuario", example = "3001234567")
        @Size(max = 30) String telefono
) {
}
