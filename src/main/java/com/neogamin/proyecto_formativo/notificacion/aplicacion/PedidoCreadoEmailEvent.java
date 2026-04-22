package com.neogamin.proyecto_formativo.notificacion.aplicacion;

import java.math.BigDecimal;

public record PedidoCreadoEmailEvent(
        Long pedidoId,
        String numeroPedido,
        String nombreUsuario,
        String emailUsuario,
        BigDecimal total,
        String moneda
) {
}
