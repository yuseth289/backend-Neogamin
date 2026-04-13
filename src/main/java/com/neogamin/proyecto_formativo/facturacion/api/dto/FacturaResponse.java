package com.neogamin.proyecto_formativo.facturacion.api.dto;

import java.math.BigDecimal;

public record FacturaResponse(
        Long id,
        Long pedidoId,
        String numeroFactura,
        String estadoFactura,
        BigDecimal subtotal,
        BigDecimal impuesto,
        BigDecimal costoEnvio,
        BigDecimal totalNeto,
        String moneda
) {
}
