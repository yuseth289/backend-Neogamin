package com.neogamin.proyecto_formativo.resena.api.dto;

import java.math.BigDecimal;

public record ResumenCalificacionProductoResponse(
        Long productoId,
        BigDecimal promedioCalificacion,
        Long totalResenas,
        Long totalCincoEstrellas,
        Long totalCuatroEstrellas,
        Long totalTresEstrellas,
        Long totalDosEstrellas,
        Long totalUnaEstrella
) {
}
