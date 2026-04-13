package com.neogamin.proyecto_formativo.carrito.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ActualizarCantidadCarritoRequest(
        @NotNull @Min(1) Integer cantidad
) {
}
