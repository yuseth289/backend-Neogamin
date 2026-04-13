package com.neogamin.proyecto_formativo.pedido.aplicacion;

import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoListadoProductoResponse;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoListadoResponse;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    public PedidoListadoResponse toListadoResponse(Pedido pedido) {
        var productos = pedido.getDetalles().stream()
                .map(detalle -> new PedidoListadoProductoResponse(
                        detalle.getProducto().getId(),
                        detalle.getProductoSku(),
                        detalle.getProductoNombre(),
                        detalle.getCantidad()
                ))
                .toList();

        int cantidadItems = pedido.getDetalles().stream()
                .mapToInt(detalle -> detalle.getCantidad() == null ? 0 : detalle.getCantidad())
                .sum();

        return new PedidoListadoResponse(
                pedido.getId(),
                pedido.getEstado().name(),
                pedido.getTotal(),
                pedido.getFechaCreacion(),
                cantidadItems,
                productos
        );
    }
}
