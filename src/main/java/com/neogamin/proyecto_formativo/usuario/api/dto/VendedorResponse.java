package com.neogamin.proyecto_formativo.usuario.api.dto;

public record VendedorResponse(
        Long id,
        Long usuarioId,
        String rolUsuario,
        String nombreCompletoORazonSocial,
        String tipoDocumento,
        String numeroDocumento,
        String pais,
        String telefono,
        String correo,
        String nombreComercial,
        Boolean aceptaTerminos,
        DatosPagoResponse datosPago
) {
}
