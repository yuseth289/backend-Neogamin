package com.neogamin.proyecto_formativo.catalogo.api.dto;

public record ProductoImagenResponse(
        Long id,
        Long productoId,
        String urlImagen,
        String altText,
        Integer orden,
        Boolean principal
) {
}
