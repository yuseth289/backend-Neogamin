package com.neogamin.proyecto_formativo.catalogo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CrearOfertaRequest(
        @NotNull Long productoId,
        @NotBlank @Size(max = 150) String titulo,
        @Size(max = 255) String descripcion,
        @DecimalMin("0.0") BigDecimal porcentajeDesc,
        @DecimalMin("0.0") BigDecimal precioOferta,
        @NotNull OffsetDateTime fechaInicio,
        @NotNull OffsetDateTime fechaFin,
        @NotBlank String estado
) {
}
