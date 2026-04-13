package com.neogamin.proyecto_formativo.interaccion.api.dto;

public record EstadoInteraccionResponse(
        Long productoId,
        boolean liked,
        boolean deseado
) {
}
