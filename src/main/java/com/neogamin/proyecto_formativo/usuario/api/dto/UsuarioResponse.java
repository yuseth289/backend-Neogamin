package com.neogamin.proyecto_formativo.usuario.api.dto;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String numeroDocumento,
        String rol,
        String estado
) {
}
