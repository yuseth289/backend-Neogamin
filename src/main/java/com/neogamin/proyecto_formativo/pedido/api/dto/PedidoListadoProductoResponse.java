package com.neogamin.proyecto_formativo.pedido.api.dto;

public record PedidoListadoProductoResponse(
        Long idProducto,
        String sku,
        String nombre,
        Integer cantidad
) {
}
