package com.neogamin.proyecto_formativo.analitica.api.dto;

import java.math.BigDecimal;

public record TopProductoResponse(
        Long idProducto,
        String nombre,
        String sku,
        String slug,
        Long unidadesVendidas,
        BigDecimal ingresosGenerados
) {
}
