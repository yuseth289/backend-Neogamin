package com.neogamin.proyecto_formativo.inventario.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorio;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.inventario.api.dto.AjustarStockProductoRequest;
import com.neogamin.proyecto_formativo.inventario.api.dto.StockProductoResponse;
import com.neogamin.proyecto_formativo.inventario.dominio.ProductoStockMovimientoEntidad;
import com.neogamin.proyecto_formativo.inventario.dominio.TipoMovimientoStock;
import com.neogamin.proyecto_formativo.inventario.infraestructura.ProductoStockMovimientoRepositorio;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioStock {

    private final ProductoRepositorio productoRepositorio;
    private final ProductoStockMovimientoRepositorio productoStockMovimientoRepositorio;

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public StockProductoResponse ajustarStock(Long idProducto, AjustarStockProductoRequest request) {
        var producto = productoRepositorio.findByIdParaActualizacion(idProducto)
                .orElseThrow(() -> new NotFoundException("El producto indicado no existe"));

        var stockFisicoAnterior = producto.getStockFisico();
        var stockReservadoAnterior = producto.getStockReservado();
        validarStockFisico(request.stockFisico(), stockReservadoAnterior);

        if (stockFisicoAnterior.equals(request.stockFisico())) {
            return new StockProductoResponse(
                    producto.getId(),
                    stockFisicoAnterior,
                    stockFisicoAnterior,
                    stockReservadoAnterior,
                    stockReservadoAnterior,
                    TipoMovimientoStock.AJUSTE.name(),
                    normalizarMotivo(request.motivo()),
                    OffsetDateTime.now()
            );
        }

        producto.setStockFisico(request.stockFisico());
        var fechaMovimiento = OffsetDateTime.now();

        var movimiento = new ProductoStockMovimientoEntidad();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(TipoMovimientoStock.AJUSTE);
        movimiento.setCantidad(Math.abs(request.stockFisico() - stockFisicoAnterior));
        movimiento.setStockFisicoAnterior(stockFisicoAnterior);
        movimiento.setStockFisicoNuevo(request.stockFisico());
        movimiento.setStockReservadoAnterior(stockReservadoAnterior);
        movimiento.setStockReservadoNuevo(stockReservadoAnterior);
        movimiento.setMotivo(normalizarMotivo(request.motivo()));
        movimiento.setFechaMovimiento(fechaMovimiento);

        productoStockMovimientoRepositorio.save(movimiento);

        return new StockProductoResponse(
                producto.getId(),
                stockFisicoAnterior,
                request.stockFisico(),
                stockReservadoAnterior,
                stockReservadoAnterior,
                TipoMovimientoStock.AJUSTE.name(),
                movimiento.getMotivo(),
                fechaMovimiento
        );
    }

    private void validarStockFisico(Integer stockFisicoNuevo, Integer stockReservadoActual) {
        if (stockFisicoNuevo < stockReservadoActual) {
            throw new BadRequestException("El stock físico no puede ser menor al stock reservado actual");
        }
    }

    private String normalizarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return "Ajuste manual de stock";
        }
        return motivo.trim();
    }
}
