package com.neogamin.proyecto_formativo.analitica.api.dto;

import java.math.BigDecimal;

public record TopVendedorResponse(
        Long idVendedor,
        String nombreVendedor,
        String emailVendedor,
        Long pedidosVendidos,
        BigDecimal ingresosGenerados
) {
}
