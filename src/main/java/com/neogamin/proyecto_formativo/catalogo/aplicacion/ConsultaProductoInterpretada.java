package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.infraestructura.ConsultaBusquedaProducto;
import java.util.List;

public record ConsultaProductoInterpretada(
        String textoOriginal,
        String textoNormalizado,
        List<String> terminos,
        List<String> aliasTipoProducto,
        boolean buscarSoloConOferta,
        List<String> marcasDetectadas,
        List<String> atributosDetectados,
        ConsultaBusquedaProducto.IntencionPrecio intencionPrecio
) {

    public String textoConsultaFts() {
        if (terminos == null || terminos.isEmpty()) {
            return "";
        }
        return terminos.stream()
                .map(this::aTerminoTsQuery)
                .filter(termino -> !termino.isBlank())
                .distinct()
                .reduce((izquierda, derecha) -> izquierda + " | " + derecha)
                .orElse("");
    }

    private String aTerminoTsQuery(String termino) {
        var limpio = termino == null ? "" : termino.trim().replaceAll("\\s+", " ");
        if (limpio.isBlank()) {
            return "";
        }
        if (limpio.contains(" ")) {
            return String.join(" <-> ", limpio.split(" "));
        }
        return limpio;
    }
}
