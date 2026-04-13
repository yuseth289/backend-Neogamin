package com.neogamin.proyecto_formativo.checkout.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProcesarPagoResponse(
        Long pedidoId,
        Long pagoId,
        String numeroPedido,
        String estadoPedido,
        String estadoPago,
        String metodoPago,
        String mensaje,
        BigDecimal total,
        OffsetDateTime fechaPedido,
        OffsetDateTime fechaEstimadaEntrega
) {
}
