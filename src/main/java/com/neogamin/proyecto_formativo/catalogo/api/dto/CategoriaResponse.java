package com.neogamin.proyecto_formativo.catalogo.api.dto;

public record CategoriaResponse(
        Long id,
        Long categoriaPadreId,
        String nombre,
        String slug,
        String descripcion,
        String estado
) {
}
