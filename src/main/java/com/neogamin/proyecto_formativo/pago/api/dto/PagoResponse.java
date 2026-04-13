package com.neogamin.proyecto_formativo.pago.api.dto;

import java.math.BigDecimal;

public record PagoResponse(
        Long id,
        Long pedidoId,
        String estado,
        String proveedorPago,
        String referenciaInterna,
        BigDecimal monto,
        String moneda,
        String tipoPago
) {
}
