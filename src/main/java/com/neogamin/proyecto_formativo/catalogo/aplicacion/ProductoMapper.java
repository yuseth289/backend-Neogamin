package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.CategoriaProductoResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaVigenteProductoResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoDetalleResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoImagenResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoListadoResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.VendedorProductoResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.OfertaEntidad;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.resena.infraestructura.ResumenResenaProducto;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    private final ProductoImagenMapper productoImagenMapper;

    public ProductoMapper(ProductoImagenMapper productoImagenMapper) {
        this.productoImagenMapper = productoImagenMapper;
    }

    public ProductoResponse toResponse(ProductoEntidad producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getSku(),
                producto.getSlug(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getMoneda(),
                producto.getPrecioLista(),
                producto.getPrecioVigenteCache(),
                producto.getStockFisico(),
                producto.getStockReservado(),
                producto.getCondicion(),
                producto.getEstado().name(),
                producto.getCategoria().getId(),
                producto.getVendedor().getId()
        );
    }

    public ProductoListadoResponse toListadoResponse(ProductoEntidad producto) {
        var urlImagenPrincipal = producto.getImagenes().stream()
                .filter(imagen -> imagen.getDeletedAt() == null)
                .filter(imagen -> Boolean.TRUE.equals(imagen.getPrincipal()))
                .map(imagen -> imagen.getUrlImagen())
                .findFirst()
                .orElse(null);

        return new ProductoListadoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getSku(),
                producto.getSlug(),
                producto.getPrecioLista(),
                producto.getPrecioVigenteCache(),
                producto.getMoneda(),
                producto.getStockFisico() - producto.getStockReservado(),
                producto.getEstado().name(),
                producto.getCategoria().getNombre(),
                producto.getVendedor().getNombre(),
                urlImagenPrincipal
        );
    }

    public ProductoDetalleResponse toDetalleResponse(
            ProductoEntidad producto,
            OfertaEntidad ofertaVigente,
            ResumenResenaProducto resumenResenas
    ) {
        List<ProductoImagenResponse> imagenes = producto.getImagenes().stream()
                .filter(imagen -> imagen.getDeletedAt() == null)
                .sorted(Comparator.comparing(imagen -> imagen.getOrden() == null ? Integer.MAX_VALUE : imagen.getOrden()))
                .map(productoImagenMapper::toResponse)
                .toList();

        return new ProductoDetalleResponse(
                producto.getId(),
                producto.getSku(),
                producto.getSlug(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getMoneda(),
                producto.getPrecioLista(),
                producto.getPrecioVigenteCache(),
                producto.getStockFisico(),
                producto.getStockReservado(),
                producto.getStockFisico() - producto.getStockReservado(),
                producto.getCondicion(),
                producto.getEstado().name(),
                new CategoriaProductoResponse(
                        producto.getCategoria().getId(),
                        producto.getCategoria().getNombre(),
                        producto.getCategoria().getSlug()
                ),
                new VendedorProductoResponse(
                        producto.getVendedor().getId(),
                        producto.getVendedor().getNombre(),
                        producto.getVendedor().getEmail()
                ),
                imagenes,
                toOfertaVigenteResponse(ofertaVigente),
                resumenResenas != null && resumenResenas.getPromedioCalificacion() != null
                        ? resumenResenas.getPromedioCalificacion()
                        : BigDecimal.ZERO,
                resumenResenas != null && resumenResenas.getTotalResenas() != null
                        ? resumenResenas.getTotalResenas()
                        : 0L
        );
    }

    private OfertaVigenteProductoResponse toOfertaVigenteResponse(OfertaEntidad oferta) {
        if (oferta == null) {
            return null;
        }
        return new OfertaVigenteProductoResponse(
                oferta.getId(),
                oferta.getTitulo(),
                oferta.getDescripcion(),
                oferta.getPorcentajeDesc(),
                oferta.getPrecioOferta(),
                oferta.getFechaInicio(),
                oferta.getFechaFin(),
                oferta.getEstado().name()
        );
    }
}
