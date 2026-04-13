package com.neogamin.proyecto_formativo.inventario.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.inventario.dominio.ProductoStockMovimientoEntidad;
import com.neogamin.proyecto_formativo.inventario.dominio.TipoMovimientoStock;
import com.neogamin.proyecto_formativo.inventario.infraestructura.ProductoStockMovimientoRepositorio;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventarioServicio {

    private final ProductoRepositorio productoRepositorio;
    private final ProductoStockMovimientoRepositorio productoStockMovimientoRepositorio;

    @Transactional
    public void reservarStock(Pedido pedido) {
        pedido.getDetalles().forEach(detalle -> {
            var producto = productoRepositorio.findByIdParaActualizacion(detalle.getProducto().getId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado para reservar stock"));
            var disponible = producto.getStockFisico() - producto.getStockReservado();
            if (disponible < detalle.getCantidad()) {
                throw new BadRequestException("Stock insuficiente para el producto " + producto.getNombre());
            }

            var reservadoAnterior = producto.getStockReservado();
            producto.setStockReservado(reservadoAnterior + detalle.getCantidad());
            registrarMovimiento(producto, pedido, TipoMovimientoStock.RESERVA, detalle.getCantidad(),
                    producto.getStockFisico(), producto.getStockFisico(), reservadoAnterior, producto.getStockReservado(),
                    "Reserva por checkout");
        });
    }

    @Transactional
    public void confirmarStock(Pedido pedido) {
        pedido.getDetalles().forEach(detalle -> {
            var producto = productoRepositorio.findByIdParaActualizacion(detalle.getProducto().getId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado para confirmar stock"));
            if (producto.getStockReservado() < detalle.getCantidad()) {
                throw new BadRequestException("Stock reservado insuficiente para el producto " + producto.getNombre());
            }

            var fisicoAnterior = producto.getStockFisico();
            var reservadoAnterior = producto.getStockReservado();
            producto.setStockFisico(fisicoAnterior - detalle.getCantidad());
            producto.setStockReservado(reservadoAnterior - detalle.getCantidad());
            registrarMovimiento(producto, pedido, TipoMovimientoStock.SALIDA, detalle.getCantidad(),
                    fisicoAnterior, producto.getStockFisico(), reservadoAnterior, producto.getStockReservado(),
                    "Confirmación por pago aprobado");
        });
    }

    @Transactional
    public void liberarStock(Pedido pedido) {
        pedido.getDetalles().forEach(detalle -> {
            var producto = productoRepositorio.findByIdParaActualizacion(detalle.getProducto().getId())
                    .orElseThrow(() -> new NotFoundException("Producto no encontrado para liberar stock"));
            var reservadoAnterior = producto.getStockReservado();
            producto.setStockReservado(Math.max(producto.getStockReservado() - detalle.getCantidad(), 0));
            registrarMovimiento(producto, pedido, TipoMovimientoStock.LIBERACION, detalle.getCantidad(),
                    producto.getStockFisico(), producto.getStockFisico(), reservadoAnterior, producto.getStockReservado(),
                    "Liberación por pago rechazado");
        });
    }

    private void registrarMovimiento(
            ProductoEntidad producto,
            Pedido pedido,
            TipoMovimientoStock tipo,
            int cantidad,
            int sfAnterior,
            int sfNuevo,
            int srAnterior,
            int srNuevo,
            String motivo
    ) {
        var movimiento = new ProductoStockMovimientoEntidad();
        movimiento.setProducto(producto);
        movimiento.setPedido(pedido);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setStockFisicoAnterior(sfAnterior);
        movimiento.setStockFisicoNuevo(sfNuevo);
        movimiento.setStockReservadoAnterior(srAnterior);
        movimiento.setStockReservadoNuevo(srNuevo);
        movimiento.setMotivo(motivo);
        movimiento.setFechaMovimiento(OffsetDateTime.now());
        productoStockMovimientoRepositorio.save(movimiento);
    }
}
