package com.neogamin.proyecto_formativo.resena.api.dto;

import java.time.OffsetDateTime;

public record ResenaResponse(
        Long id,
        Long productoId,
        Long usuarioId,
        boolean compraVerificada,
        Short calificacion,
        String comentario,
        OffsetDateTime fecha
) {
}
