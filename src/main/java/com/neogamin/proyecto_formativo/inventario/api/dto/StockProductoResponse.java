package com.neogamin.proyecto_formativo.inventario.api.dto;

import java.time.OffsetDateTime;

public record StockProductoResponse(
        Long productoId,
        Integer stockFisicoAnterior,
        Integer stockFisicoNuevo,
        Integer stockReservadoAnterior,
        Integer stockReservadoNuevo,
        String tipoMovimiento,
        String motivo,
        OffsetDateTime fechaMovimiento
) {
}
