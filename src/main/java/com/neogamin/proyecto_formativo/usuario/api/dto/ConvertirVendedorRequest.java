package com.neogamin.proyecto_formativo.usuario.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ConvertirVendedorRequest(
        @NotBlank @Size(max = 150) String nombreCompletoORazonSocial,
        @NotBlank @Size(max = 30) String tipoDocumento,
        @NotBlank @Size(max = 30) String numeroDocumento,
        @NotBlank @Size(max = 80) String pais,
        @NotBlank
        @Pattern(regexp = "^[+0-9()\\-\\s]{7,20}$", message = "El telefono no tiene un formato valido")
        String telefono,
        @Email @NotBlank @Size(max = 190) String correo,
        @NotBlank @Size(max = 150) String nombreComercial,
        @NotNull
        @AssertTrue(message = "Debes aceptar los terminos para convertirte en vendedor")
        Boolean aceptaTerminos,
        @Valid DatosPagoRequest datosPago
) {
}
