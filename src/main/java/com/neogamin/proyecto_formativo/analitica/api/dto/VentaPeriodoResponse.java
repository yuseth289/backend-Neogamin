package com.neogamin.proyecto_formativo.analitica.api.dto;

import java.math.BigDecimal;

public record VentaPeriodoResponse(
        String periodo,
        BigDecimal ingresos,
        Long cantidadPedidos
) {
}
