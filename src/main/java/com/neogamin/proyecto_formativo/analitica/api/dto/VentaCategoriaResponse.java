package com.neogamin.proyecto_formativo.analitica.api.dto;

import java.math.BigDecimal;

public record VentaCategoriaResponse(
        Long idCategoria,
        String nombreCategoria,
        Long unidadesVendidas,
        BigDecimal ingresosGenerados
) {
}
