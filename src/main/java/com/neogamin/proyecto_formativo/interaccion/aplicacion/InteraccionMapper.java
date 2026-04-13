package com.neogamin.proyecto_formativo.interaccion.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoListadoResponse;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoImagenRepositorio;
import com.neogamin.proyecto_formativo.interaccion.api.dto.WishlistProductoResponse;
import com.neogamin.proyecto_formativo.interaccion.dominio.ProductoDeseado;
import org.springframework.stereotype.Component;

@Component
public class InteraccionMapper {

    private final ProductoImagenRepositorio productoImagenRepositorio;

    public InteraccionMapper(ProductoImagenRepositorio productoImagenRepositorio) {
        this.productoImagenRepositorio = productoImagenRepositorio;
    }

    public WishlistProductoResponse toWishlistResponse(ProductoDeseado deseado) {
        var producto = deseado.getProducto();
        var urlImagenPrincipal = productoImagenRepositorio.findByProductoIdAndDeletedAtIsNullOrderByOrdenAsc(producto.getId())
                .stream()
                .filter(imagen -> Boolean.TRUE.equals(imagen.getPrincipal()))
                .map(imagen -> imagen.getUrlImagen())
                .findFirst()
                .orElse(null);

        return new WishlistProductoResponse(
                new ProductoListadoResponse(
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
                ),
                deseado.getFechaAgregado()
        );
    }
}
