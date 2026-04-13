package com.neogamin.proyecto_formativo.resena.api.dto;

public record DistribucionEstrellasResponse(
        Long totalCincoEstrellas,
        Long totalCuatroEstrellas,
        Long totalTresEstrellas,
        Long totalDosEstrellas,
        Long totalUnaEstrella
) {
}
