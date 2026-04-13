package com.neogamin.proyecto_formativo.usuario.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DatosPagoRequest(
        @NotBlank @Size(max = 20) String tipoCuenta,
        @NotBlank @Size(max = 40) String numeroCuenta,
        @NotBlank @Size(max = 120) String banco,
        @NotBlank @Size(max = 150) String titularCuenta
) {
}
