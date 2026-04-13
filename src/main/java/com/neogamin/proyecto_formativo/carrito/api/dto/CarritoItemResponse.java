package com.neogamin.proyecto_formativo.carrito.api.dto;

import java.math.BigDecimal;

public record CarritoItemResponse(
        Long idItem,
        Long idProducto,
        String sku,
        String slug,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String moneda
) {
}
