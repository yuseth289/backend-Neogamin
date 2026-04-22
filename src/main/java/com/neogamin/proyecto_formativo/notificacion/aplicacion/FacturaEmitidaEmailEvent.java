package com.neogamin.proyecto_formativo.notificacion.aplicacion;

import java.math.BigDecimal;

public record FacturaEmitidaEmailEvent(
        Long facturaId,
        Long pedidoId,
        String numeroFactura,
        String numeroPedido,
        String nombreUsuario,
        String emailUsuario,
        BigDecimal total,
        String moneda
) {
}
