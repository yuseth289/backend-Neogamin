package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoImagenResponse;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoImagenEntidad;
import org.springframework.stereotype.Component;

@Component
public class ProductoImagenMapper {

    public ProductoImagenResponse toResponse(ProductoImagenEntidad imagen) {
        return new ProductoImagenResponse(
                imagen.getId(),
                imagen.getProducto().getId(),
                imagen.getUrlImagen(),
                imagen.getAltText(),
                imagen.getOrden(),
                imagen.getPrincipal()
        );
    }
}
