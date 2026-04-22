package com.neogamin.proyecto_formativo.notificacion.aplicacion;

public record UsuarioInicioSesionEmailEvent(
        Long usuarioId,
        String nombre,
        String email
) {
}
