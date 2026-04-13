package com.neogamin.proyecto_formativo.catalogo.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarStockProductoRequest(
        @NotNull @Min(0) Integer stockFisico,
        String motivo
) {
}
