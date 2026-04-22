package com.neogamin.proyecto_formativo.notificacion.aplicacion;

public record UsuarioRegistradoEmailEvent(
        Long usuarioId,
        String nombre,
        String email
) {
}
