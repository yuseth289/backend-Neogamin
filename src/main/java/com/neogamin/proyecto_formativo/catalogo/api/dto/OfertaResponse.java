package com.neogamin.proyecto_formativo.catalogo.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OfertaResponse(
        Long id,
        Long productoId,
        String titulo,
        String descripcion,
        BigDecimal porcentajeDesc,
        BigDecimal precioOferta,
        OffsetDateTime fechaInicio,
        OffsetDateTime fechaFin,
        String estado
) {
}
