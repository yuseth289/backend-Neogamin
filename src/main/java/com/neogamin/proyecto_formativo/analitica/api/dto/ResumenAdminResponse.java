package com.neogamin.proyecto_formativo.analitica.api.dto;

import java.math.BigDecimal;

public record ResumenAdminResponse(
        BigDecimal ingresosTotales,
        Long pedidosTotales,
        BigDecimal ticketPromedioGlobal,
        Long cantidadClientesActivos
) {
}
