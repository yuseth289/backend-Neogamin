package com.neogamin.proyecto_formativo.checkout.api.dto;

import java.math.BigDecimal;

public record ItemResumenCheckoutResponse(
        Long idProducto,
        String sku,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
