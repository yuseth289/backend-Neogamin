package com.neogamin.proyecto_formativo.inventario.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AjustarStockProductoRequest(
        @NotNull @Min(0) Integer stockFisico,
        @Size(max = 255) String motivo
) {
}
