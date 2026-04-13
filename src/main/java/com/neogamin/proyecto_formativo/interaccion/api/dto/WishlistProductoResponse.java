package com.neogamin.proyecto_formativo.interaccion.api.dto;

import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoListadoResponse;
import java.time.OffsetDateTime;

public record WishlistProductoResponse(
        ProductoListadoResponse producto,
        OffsetDateTime fechaAgregado
) {
}
