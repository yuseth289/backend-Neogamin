package com.neogamin.proyecto_formativo.analitica.api.dto;

public record StockBajoResponse(
        Long idProducto,
        String nombre,
        String sku,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible
) {
}
