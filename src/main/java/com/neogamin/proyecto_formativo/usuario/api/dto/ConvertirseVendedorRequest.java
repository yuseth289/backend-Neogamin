package com.neogamin.proyecto_formativo.usuario.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConvertirseVendedorRequest(
        @NotBlank
        @Size(max = 30)
        String numeroDocumento
) {
}
