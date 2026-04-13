package com.neogamin.proyecto_formativo.checkout.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DireccionCheckoutRequest(
        @NotBlank String nombreCompleto,
        @NotBlank String correoElectronico,
        @NotBlank String telefono,
        @NotBlank String direccion,
        String apartamentoInterior,
        @NotBlank String ciudad,
        @NotBlank String estadoRegion,
        String codigoPostal,
        @NotBlank String pais,
        String referenciaEntrega
) {
}
