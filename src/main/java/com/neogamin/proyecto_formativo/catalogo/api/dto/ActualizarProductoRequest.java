package com.neogamin.proyecto_formativo.catalogo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ActualizarProductoRequest(
        @NotNull Long categoriaId,
        @NotNull Long vendedorId,
        @NotBlank @Size(max = 80) String sku,
        @NotBlank @Size(max = 180) String slug,
        @NotBlank @Size(max = 180) String nombre,
        String descripcion,
        @NotBlank @Size(min = 3, max = 3) String moneda,
        @NotNull @DecimalMin("0.0") BigDecimal precioLista,
        @NotNull @Min(0) Integer stockFisico,
        String condicion,
        @NotBlank String estado
) {
}
