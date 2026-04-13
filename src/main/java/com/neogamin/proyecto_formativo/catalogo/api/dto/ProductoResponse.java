package com.neogamin.proyecto_formativo.catalogo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record ProductoResponse(
        @Schema(description = "Identificador del producto", example = "11")
        Long id,
        @Schema(description = "SKU del producto", example = "SKU-SM-200")
        String sku,
        @Schema(description = "Slug del producto", example = "xiaomi-redmi-note-13-pro")
        String slug,
        @Schema(description = "Nombre del producto", example = "Xiaomi Redmi Note 13 Pro")
        String nombre,
        @Schema(description = "Descripcion del producto", example = "Smartphone 256GB, 8GB RAM, pantalla AMOLED 120Hz")
        String descripcion,
        @Schema(description = "Codigo ISO de moneda", example = "COP")
        String moneda,
        @Schema(description = "Precio de lista", example = "1599000.00")
        BigDecimal precioLista,
        @Schema(description = "Precio vigente cacheado", example = "1499000.00")
        BigDecimal precioVigente,
        @Schema(description = "Stock fisico", example = "25")
        Integer stockFisico,
        @Schema(description = "Stock reservado", example = "0")
        Integer stockReservado,
        @Schema(description = "Condicion del producto", example = "nuevo")
        String condicion,
        @Schema(description = "Estado actual del producto", example = "ACTIVO")
        String estado,
        @Schema(description = "Categoria asociada", example = "4")
        Long categoriaId,
        @Schema(description = "Vendedor propietario", example = "2")
        Long vendedorId
) {
}
