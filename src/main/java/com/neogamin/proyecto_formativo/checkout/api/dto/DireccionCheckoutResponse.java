package com.neogamin.proyecto_formativo.checkout.api.dto;

public record DireccionCheckoutResponse(
        String nombreCompleto,
        String correoElectronico,
        String telefono,
        String direccion,
        String apartamentoInterior,
        String ciudad,
        String estadoRegion,
        String codigoPostal,
        String pais,
        String referenciaEntrega
) {
}
