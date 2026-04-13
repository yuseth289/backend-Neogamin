package com.neogamin.proyecto_formativo.usuario.api.dto;

public record LoginResponse(
        String token,
        Long usuarioId,
        String nombre,
        String email,
        String rol
) {
}
