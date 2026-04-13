package com.neogamin.proyecto_formativo.catalogo.api.dto;

import java.math.BigDecimal;

public record OfertaProductoResumenResponse(
        Long idProducto,
        String nombre,
        String sku,
        String slug,
        BigDecimal precioLista,
        BigDecimal precioVigente,
        String moneda
) {
}
