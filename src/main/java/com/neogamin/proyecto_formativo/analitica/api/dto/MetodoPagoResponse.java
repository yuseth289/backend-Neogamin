package com.neogamin.proyecto_formativo.analitica.api.dto;

import java.math.BigDecimal;

public record MetodoPagoResponse(
        String metodoPago,
        Long cantidadUsos,
        BigDecimal montoTotal
) {
}
