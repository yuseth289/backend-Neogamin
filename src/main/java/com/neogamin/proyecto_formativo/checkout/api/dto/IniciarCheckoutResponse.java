package com.neogamin.proyecto_formativo.checkout.api.dto;

import java.util.List;

public record IniciarCheckoutResponse(
        Long pedidoId,
        String numeroPedido,
        String estadoPedido,
        ResumenCheckoutResponse resumen,
        List<ItemResumenCheckoutResponse> items,
        DireccionCheckoutResponse direccionEnvio,
        DireccionCheckoutResponse direccionFactura
) {
}
