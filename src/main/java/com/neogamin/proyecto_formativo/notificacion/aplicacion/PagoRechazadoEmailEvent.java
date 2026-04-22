package com.neogamin.proyecto_formativo.notificacion.aplicacion;

import java.math.BigDecimal;

public record PagoRechazadoEmailEvent(
        Long pagoId,
        Long pedidoId,
        String numeroPedido,
        String nombreUsuario,
        String emailUsuario,
        BigDecimal monto,
        String moneda,
        String tipoPago
) {
}
