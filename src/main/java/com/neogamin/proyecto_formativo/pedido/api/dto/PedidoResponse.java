package com.neogamin.proyecto_formativo.pedido.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long usuarioId,
        String estado,
        String moneda,
        BigDecimal subtotal,
        BigDecimal impuesto,
        BigDecimal costoEnvio,
        BigDecimal total,
        boolean needsRecalc,
        List<PedidoItemResponse> items
) {
}
