package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import java.util.List;

public record ConsultaBusquedaProducto(
        String textoOriginal,
        String textoNormalizado,
        String textoConsultaFts,
        List<String> terminos,
        List<String> aliasTipoProducto,
        boolean buscarSoloConOferta,
        boolean soloDisponibles,
        int page,
        int size,
        IntencionPrecio intencionPrecio
) {
    public enum IntencionPrecio {
        NEUTRO,
        BAJO,
        ALTO
    }
}
