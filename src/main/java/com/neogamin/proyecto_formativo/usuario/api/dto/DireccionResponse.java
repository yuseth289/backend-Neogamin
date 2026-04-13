package com.neogamin.proyecto_formativo.usuario.api.dto;

public record DireccionResponse(
        Long id,
        String tipo,
        boolean esPrincipal,
        String pais,
        String departamento,
        String ciudad,
        String comuna,
        String codigoPostal,
        String calle,
        String numero,
        String referencia,
        String estado
) {
}
