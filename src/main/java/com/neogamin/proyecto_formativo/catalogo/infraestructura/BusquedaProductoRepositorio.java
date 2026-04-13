package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import org.springframework.data.domain.Page;

public interface BusquedaProductoRepositorio {

    Page<ResultadoBusquedaProductoFila> buscar(ConsultaBusquedaProducto consulta);
}
