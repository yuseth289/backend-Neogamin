package com.neogamin.proyecto_formativo.resena.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CrearOActualizarResenaRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) @Max(5) Short calificacion,
        String comentario
) {
}
