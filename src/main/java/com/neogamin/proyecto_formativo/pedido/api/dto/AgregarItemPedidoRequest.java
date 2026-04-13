package com.neogamin.proyecto_formativo.pedido.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AgregarItemPedidoRequest(
        @NotNull Long productoId,
        @NotNull @Min(1) Integer cantidad,
        BigDecimal descuentoUnitario,
        BigDecimal impuestoUnitario
) {
}
