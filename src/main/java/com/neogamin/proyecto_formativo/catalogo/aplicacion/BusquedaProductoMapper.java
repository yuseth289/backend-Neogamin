package com.neogamin.proyecto_formativo.catalogo.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoBusquedaResponse;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ResultadoBusquedaProductoFila;
import org.springframework.stereotype.Component;

@Component
public class BusquedaProductoMapper {

    public ProductoBusquedaResponse toResponse(ResultadoBusquedaProductoFila fila) {
        return new ProductoBusquedaResponse(
                fila.idProducto(),
                fila.nombre(),
                fila.sku(),
                fila.slug(),
                fila.precioLista(),
                fila.precioVigente(),
                fila.moneda(),
                fila.stockDisponible(),
                fila.nombreCategoria(),
                fila.urlImagenPrincipal(),
                fila.puntajeRelevancia()
        );
    }
}
