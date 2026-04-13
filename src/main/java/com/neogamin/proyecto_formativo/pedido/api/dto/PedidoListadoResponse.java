package com.neogamin.proyecto_formativo.pedido.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PedidoListadoResponse(
        Long idPedido,
        String estado,
        BigDecimal total,
        OffsetDateTime fechaCreacion,
        Integer cantidadItems,
        List<PedidoListadoProductoResponse> productos
) {
}
