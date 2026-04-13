package com.neogamin.proyecto_formativo.checkout.api.dto;

import jakarta.validation.Valid;

public record GuardarEnvioRequest(
        Long pedidoId,
        Long direccionEnvioId,
        Long direccionFacturaId,
        @Valid DireccionCheckoutRequest direccionEnvio,
        @Valid DireccionCheckoutRequest direccionFactura,
        Boolean mismaDireccionFacturacion
) {
}
