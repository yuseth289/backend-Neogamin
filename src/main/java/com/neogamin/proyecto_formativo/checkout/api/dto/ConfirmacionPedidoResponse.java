package com.neogamin.proyecto_formativo.checkout.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ConfirmacionPedidoResponse(
        String mensaje,
        Long pedidoId,
        String numeroPedido,
        String estadoPedido,
        String estadoPago,
        String metodoPago,
        BigDecimal totalPagado,
        Integer cantidadItems,
        OffsetDateTime fechaPedido,
        OffsetDateTime fechaEstimadaEntrega,
        String numeroFactura,
        DireccionCheckoutResponse direccionEnvio,
        DireccionCheckoutResponse direccionFactura,
        ResumenCheckoutResponse resumen,
        List<ItemResumenCheckoutResponse> items
) {
}
