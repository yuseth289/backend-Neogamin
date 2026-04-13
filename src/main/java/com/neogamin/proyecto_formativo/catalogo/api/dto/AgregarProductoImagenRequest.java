package com.neogamin.proyecto_formativo.catalogo.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgregarProductoImagenRequest(
        @NotBlank String urlImagen,
        @Size(max = 180) String altText,
        @NotNull @Min(1) Integer orden,
        @NotNull Boolean principal
) {
}
