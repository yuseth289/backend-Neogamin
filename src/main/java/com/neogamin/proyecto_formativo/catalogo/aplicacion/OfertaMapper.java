package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaActivaResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaProductoResumenResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.OfertaEntidad;
import org.springframework.stereotype.Component;

@Component
public class OfertaMapper {

    public OfertaResponse toResponse(OfertaEntidad oferta) {
        return new OfertaResponse(
                oferta.getId(),
                oferta.getProducto().getId(),
                oferta.getTitulo(),
                oferta.getDescripcion(),
                oferta.getPorcentajeDesc(),
                oferta.getPrecioOferta(),
                oferta.getFechaInicio(),
                oferta.getFechaFin(),
                oferta.getEstado().name()
        );
    }

    public OfertaActivaResponse toActivaResponse(OfertaEntidad oferta) {
        return new OfertaActivaResponse(
                oferta.getId(),
                oferta.getTitulo(),
                oferta.getDescripcion(),
                oferta.getPorcentajeDesc(),
                oferta.getPrecioOferta(),
                oferta.getFechaInicio(),
                oferta.getFechaFin(),
                new OfertaProductoResumenResponse(
                        oferta.getProducto().getId(),
                        oferta.getProducto().getNombre(),
                        oferta.getProducto().getSku(),
                        oferta.getProducto().getSlug(),
                        oferta.getProducto().getPrecioLista(),
                        oferta.getProducto().getPrecioVigenteCache(),
                        oferta.getProducto().getMoneda()
                )
        );
    }
}
