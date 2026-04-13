package com.neogamin.proyecto_formativo.usuario.api.dto;

import com.neogamin.proyecto_formativo.usuario.dominio.Direccion.TipoDireccion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GuardarDireccionRequest(
        @NotNull TipoDireccion tipo,
        Boolean esPrincipal,
        @NotBlank @Size(max = 80) String pais,
        @Size(max = 100) String departamento,
        @NotBlank @Size(max = 100) String ciudad,
        @Size(max = 100) String comuna,
        @Size(max = 20) String codigoPostal,
        @NotBlank @Size(max = 150) String calle,
        @NotBlank @Size(max = 30) String numero,
        @Size(max = 255) String referencia
) {
}
