package com.neogamin.proyecto_formativo.checkout.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ProcesarPagoRequest(
        @NotNull Long pedidoId,
        @Valid @NotNull MetodoPagoCheckoutRequest metodoPago,
        Boolean simularFallo
) {
}
