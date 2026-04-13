package com.neogamin.proyecto_formativo.usuario.api.dto;

public record PerfilUsuarioResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String numeroDocumento,
        String sobreMi,
        String fotoPerfilUrl,
        boolean prefiereNoticias,
        boolean prefiereOfertas,
        String rol,
        String estado
) {
}
