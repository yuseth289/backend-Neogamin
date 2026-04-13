package com.neogamin.proyecto_formativo.analitica.api.dto;

import java.math.BigDecimal;

public record ResumenVendedorResponse(
        BigDecimal ingresosTotales,
        BigDecimal ingresosMesActual,
        Long cantidadPedidosVendidos,
        BigDecimal ticketPromedio
) {
}
