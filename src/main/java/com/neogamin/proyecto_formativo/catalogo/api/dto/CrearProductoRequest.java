package com.neogamin.proyecto_formativo.catalogo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CrearProductoRequest(
        @Schema(description = "Identificador de la categoria", example = "4")
        @NotNull Long categoriaId,
        @Schema(description = "SKU unico del producto", example = "SKU-SM-200")
        @NotBlank @Size(max = 80) String sku,
        @Schema(description = "Slug unico para URLs amigables", example = "xiaomi-redmi-note-13-pro")
        @NotBlank @Size(max = 180) String slug,
        @Schema(description = "Nombre comercial del producto", example = "Xiaomi Redmi Note 13 Pro")
        @NotBlank @Size(max = 180) String nombre,
        @Schema(description = "Descripcion detallada del producto", example = "Smartphone 256GB, 8GB RAM, pantalla AMOLED 120Hz")
        String descripcion,
        @Schema(description = "Codigo ISO de moneda", example = "COP")
        @NotBlank @Size(min = 3, max = 3) String moneda,
        @Schema(description = "Precio de lista del producto", example = "1599000.00")
        @NotNull @DecimalMin("0.0") BigDecimal precioLista,
        @Schema(description = "Cantidad fisica disponible en inventario", example = "25")
        @NotNull @Min(0) Integer stockFisico,
        @Schema(description = "Condicion comercial del producto", example = "nuevo")
        String condicion
) {
}
