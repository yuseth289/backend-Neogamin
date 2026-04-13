package com.neogamin.proyecto_formativo.resena.api.dto;

import java.time.OffsetDateTime;

public record ResenaProductoResponse(
        Long id,
        Long productoId,
        Long usuarioId,
        String nombreUsuario,
        Long pedidoId,
        boolean compraVerificada,
        Short calificacion,
        String comentario,
        OffsetDateTime fecha
) {
}
