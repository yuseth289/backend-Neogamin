package com.neogamin.proyecto_formativo.usuario.api.dto;

public record DatosPagoResponse(
        String tipoCuenta,
        String numeroCuenta,
        String banco,
        String titularCuenta
) {
}
