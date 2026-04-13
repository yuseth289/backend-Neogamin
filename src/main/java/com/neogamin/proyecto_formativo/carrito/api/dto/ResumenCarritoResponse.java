package com.neogamin.proyecto_formativo.carrito.api.dto;

import java.math.BigDecimal;

public record ResumenCarritoResponse(
        Integer cantidadProductosDistintos,
        Integer cantidadUnidades,
        BigDecimal subtotal,
        BigDecimal total,
        String moneda
) {
}
