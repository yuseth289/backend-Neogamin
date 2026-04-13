package com.neogamin.proyecto_formativo.resena.infraestructura;

import java.math.BigDecimal;

public interface ResumenResenaProducto {

    BigDecimal getPromedioCalificacion();

    Long getTotalResenas();

    Long getTotalCincoEstrellas();

    Long getTotalCuatroEstrellas();

    Long getTotalTresEstrellas();

    Long getTotalDosEstrellas();

    Long getTotalUnaEstrella();
}
