package com.neogamin.proyecto_formativo.pedido.api.dto;

public record CheckoutResponse(
        Long pedidoId,
        Long pagoId,
        String estadoPedido,
        String estadoPago
) {
}
