package com.neogamin.proyecto_formativo.catalogo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ActualizarPrecioProductoRequest(
        @NotNull @DecimalMin("0.0") BigDecimal nuevoPrecio,
        String motivo
) {
}
