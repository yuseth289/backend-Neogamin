package com.neogamin.proyecto_formativo.checkout.api.dto;

import com.neogamin.proyecto_formativo.pago.dominio.TipoPago;
import jakarta.validation.constraints.NotNull;

public record MetodoPagoCheckoutRequest(
        @NotNull TipoPago tipoPago,
        String nombreTitular,
        String numeroTarjeta,
        String fechaVencimiento,
        String cvv
) {
}
