package com.neogamin.proyecto_formativo.catalogo.api.dto;

import java.math.BigDecimal;

public record ProductoListadoResponse(
        Long idProducto,
        String nombre,
        String sku,
        String slug,
        BigDecimal precioLista,
        BigDecimal precioVigente,
        String moneda,
        Integer stockDisponible,
        String estado,
        String nombreCategoria,
        String nombreVendedor,
        String urlImagenPrincipal
) {
}
