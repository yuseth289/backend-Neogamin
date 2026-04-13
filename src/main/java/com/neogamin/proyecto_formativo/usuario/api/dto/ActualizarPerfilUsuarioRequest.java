package com.neogamin.proyecto_formativo.usuario.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarPerfilUsuarioRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Email @Size(max = 190) String email,
        @Size(max = 30) String telefono,
        @Size(max = 500) String sobreMi,
        @Size(max = 500) String fotoPerfilUrl,
        Boolean prefiereNoticias,
        Boolean prefiereOfertas
) {
}
