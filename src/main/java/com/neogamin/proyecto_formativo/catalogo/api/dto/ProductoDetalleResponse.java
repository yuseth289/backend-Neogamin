package com.neogamin.proyecto_formativo.catalogo.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoDetalleResponse(
        Long idProducto,
        String sku,
        String slug,
        String nombre,
        String descripcion,
        String moneda,
        BigDecimal precioLista,
        BigDecimal precioVigente,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible,
        String condicion,
        String estado,
        CategoriaProductoResponse categoria,
        VendedorProductoResponse vendedor,
        List<ProductoImagenResponse> imagenes,
        OfertaVigenteProductoResponse ofertaVigente,
        BigDecimal ratingPromedio,
        Long totalResenas
) {
}
