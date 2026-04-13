package com.neogamin.proyecto_formativo.pedido.api.dto;

public record CrearPedidoRequest(
        String moneda,
        Long direccionEnvioId,
        Long direccionFacturaId
) {
}
