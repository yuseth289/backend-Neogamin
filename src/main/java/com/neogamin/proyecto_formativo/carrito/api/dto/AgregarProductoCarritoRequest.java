package com.neogamin.proyecto_formativo.carrito.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AgregarProductoCarritoRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad
) {
}
