package com.neogamin.proyecto_formativo.carrito.api.dto;

import java.util.List;

public record CarritoResponse(
        Long idCarrito,
        Long idUsuario,
        String estado,
        List<CarritoItemResponse> items,
        ResumenCarritoResponse resumen
) {
}
