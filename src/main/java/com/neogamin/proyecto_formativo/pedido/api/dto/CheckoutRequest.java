package com.neogamin.proyecto_formativo.pedido.api.dto;

import com.neogamin.proyecto_formativo.pago.dominio.TipoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotBlank String proveedorPago,
        String referenciaExterna,
        String idempotencyKey,
        @NotNull TipoPago tipoPago
) {
}
