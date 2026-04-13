package com.neogamin.proyecto_formativo.pedido.api.dto;

import java.math.BigDecimal;

public record PedidoItemResponse(
        Long productoId,
        String sku,
        String nombre,
        Integer cantidad,
        BigDecimal precioFinalUnitario,
        BigDecimal totalLinea
) {
}
