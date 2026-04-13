package com.neogamin.proyecto_formativo.catalogo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearCategoriaRequest(
        @Schema(description = "Identificador de la categoria padre. Puede ser null para una categoria raiz.", example = "1")
        Long categoriaPadreId,
        @Schema(description = "Nombre de la categoria", example = "Smartphones")
        @NotBlank @Size(max = 120) String nombre,
        @Schema(description = "Slug unico de la categoria", example = "smartphones")
        @NotBlank @Size(max = 150) String slug,
        @Schema(description = "Descripcion breve de la categoria", example = "Telefonos inteligentes y accesorios principales")
        @Size(max = 255) String descripcion
) {
}
