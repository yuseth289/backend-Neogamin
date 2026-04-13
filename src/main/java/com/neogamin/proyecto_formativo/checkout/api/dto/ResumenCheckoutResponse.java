package com.neogamin.proyecto_formativo.checkout.api.dto;

import java.math.BigDecimal;

public record ResumenCheckoutResponse(
        Integer cantidadItems,
        BigDecimal subtotal,
        BigDecimal impuesto,
        BigDecimal costoEnvio,
        BigDecimal total,
        String moneda
) {
}
