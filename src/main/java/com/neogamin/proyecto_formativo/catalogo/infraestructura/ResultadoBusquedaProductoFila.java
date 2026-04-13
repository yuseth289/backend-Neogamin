package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import java.math.BigDecimal;

public record ResultadoBusquedaProductoFila(
        Long idProducto,
        String nombre,
        String sku,
        String slug,
        BigDecimal precioLista,
        BigDecimal precioVigente,
        String moneda,
        Integer stockDisponible,
        String nombreCategoria,
        String urlImagenPrincipal,
        BigDecimal puntajeRelevancia
) {
}
