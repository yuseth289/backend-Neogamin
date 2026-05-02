package com.neogamin.proyecto_formativo.pago.aplicacion;

import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.facturacion.aplicacion.FacturacionServicio;
import com.neogamin.proyecto_formativo.inventario.aplicacion.InventarioServicio;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PagoAprobadoEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PagoRechazadoEmailEvent;
import com.neogamin.proyecto_formativo.pago.api.dto.PagoResponse;
import com.neogamin.proyecto_formativo.pago.dominio.EstadoPago;
import com.neogamin.proyecto_formativo.pago.dominio.Pago;
import com.neogamin.proyecto_formativo.pago.infraestructura.PagoRepositorioJpa;
import com.neogamin.proyecto_formativo.pedido.api.dto.CheckoutRequest;
import com.neogamin.proyecto_formativo.pedido.dominio.EstadoPedido;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagoServicio {

    private final PagoRepositorioJpa pagoRepositorioJpa;
    private final PedidoRepositorioJpa pedidoRepositorioJpa;
    private final InventarioServicio inventarioServicio;
    private final FacturacionServicio facturacionServicio;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Pago registrarPagoPendiente(Pedido pedido, CheckoutRequest request) {
        return pagoRepositorioJpa.findByPedidoId(pedido.getId())
                .map(pagoExistente -> actualizarPagoPendiente(pagoExistente, pedido, request))
                .orElseGet(() -> crearPagoPendiente(pedido, request));
    }

    @Transactional
    public PagoResponse aprobarPago(Long pagoId) {
        var pago = pagoRepositorioJpa.findById(pagoId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));
        var pedido = cargarPedidoDelPago(pago);
        validarPuedeGestionarPago(pedido);

        if (pago.getEstado() == EstadoPago.APROBADO || pago.getEstado() == EstadoPago.CAPTURADO) {
            return toResponse(pago);
        }
        if (pago.getEstado() != EstadoPago.PENDIENTE && pago.getEstado() != EstadoPago.AUTORIZADO) {
            throw new BadRequestException("El pago no puede aprobarse en el estado actual");
        }

        pago.setEstado(EstadoPago.APROBADO);
        pago.setFechaEvento(OffsetDateTime.now());
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setFechaPago(OffsetDateTime.now());
        inventarioServicio.confirmarStock(pedido);
        facturacionServicio.emitir(pedido, pago);
        applicationEventPublisher.publishEvent(new PagoAprobadoEmailEvent(
                pago.getId(),
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getUsuario().getNombre(),
                pedido.getUsuario().getEmail(),
                pago.getMonto(),
                pago.getMoneda(),
                pago.getTipoPago().name()
        ));
        return toResponse(pago);
    }

    @Transactional
    public PagoResponse rechazarPago(Long pagoId) {
        var pago = pagoRepositorioJpa.findById(pagoId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));
        var pedido = cargarPedidoDelPago(pago);
        validarPuedeGestionarPago(pedido);

        if (pago.getEstado() == EstadoPago.RECHAZADO || pago.getEstado() == EstadoPago.ANULADO) {
            return toResponse(pago);
        }

        pago.setEstado(EstadoPago.RECHAZADO);
        pago.setFechaEvento(OffsetDateTime.now());
        pedido.setEstado(EstadoPedido.CANCELADO);
        inventarioServicio.liberarStock(pedido);
        applicationEventPublisher.publishEvent(new PagoRechazadoEmailEvent(
                pago.getId(),
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getUsuario().getNombre(),
                pedido.getUsuario().getEmail(),
                pago.getMonto(),
                pago.getMoneda(),
                pago.getTipoPago().name()
        ));
        return toResponse(pago);
    }

    @Transactional(readOnly = true)
    public PagoResponse obtener(Long pagoId) {
        var pago = pagoRepositorioJpa.findById(pagoId)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));
        var pedido = cargarPedidoDelPago(pago);
        validarPuedeConsultarPago(pedido);
        return toResponse(pago);
    }

    private Pedido cargarPedidoDelPago(Pago pago) {
        return pedidoRepositorioJpa.findWithDetallesById(pago.getPedido().getId())
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
    }

    private void validarPuedeConsultarPago(Pedido pedido) {
        var usuario = SeguridadUtils.usuarioAutenticado();
        if (usuario.getRol() == RolUsuario.ADMIN) {
            return;
        }
        if (pedido.getUsuario().getId().equals(usuario.getId())) {
            return;
        }
        if (usuario.getRol() == RolUsuario.VENDEDOR && pedidoContieneProductoDelVendedor(pedido, usuario.getId())) {
            return;
        }
        throw new ForbiddenException("No tienes permisos para acceder a este pago");
    }

    private void validarPuedeGestionarPago(Pedido pedido) {
        var usuario = SeguridadUtils.usuarioAutenticado();
        if (usuario.getRol() == RolUsuario.ADMIN) {
            return;
        }
        if (usuario.getRol() == RolUsuario.VENDEDOR && pedidoContieneProductoDelVendedor(pedido, usuario.getId())) {
            return;
        }
        throw new ForbiddenException("No tienes permisos para gestionar este pago");
    }

    private boolean pedidoContieneProductoDelVendedor(Pedido pedido, Long vendedorId) {
        return pedido.getDetalles().stream()
                .anyMatch(detalle -> detalle.getProducto() != null
                        && detalle.getProducto().getVendedor() != null
                        && vendedorId.equals(detalle.getProducto().getVendedor().getId()));
    }

    private PagoResponse toResponse(Pago pago) {
        return new PagoResponse(
                pago.getId(),
                pago.getPedido().getId(),
                pago.getEstado().name(),
                pago.getProveedorPago(),
                pago.getReferenciaInterna(),
                pago.getMonto(),
                pago.getMoneda(),
                pago.getTipoPago().name()
        );
    }

    private Pago crearPagoPendiente(Pedido pedido, CheckoutRequest request) {
        var pago = new Pago();
        pago.setPedido(pedido);
        pago.setUsuario(pedido.getUsuario());
        pago.setReferenciaInterna("PAY-" + UUID.randomUUID());
        return actualizarPagoPendiente(pago, pedido, request);
    }

    private Pago actualizarPagoPendiente(Pago pago, Pedido pedido, CheckoutRequest request) {
        pago.setPedido(pedido);
        pago.setUsuario(pedido.getUsuario());
        pago.setProveedorPago(request.proveedorPago());
        pago.setReferenciaExterna(request.referenciaExterna());
        pago.setIdempotencyKey(request.idempotencyKey());
        pago.setMonto(pedido.getTotal());
        pago.setMoneda(pedido.getMoneda());
        pago.setTipoPago(request.tipoPago());
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setFechaEvento(OffsetDateTime.now());
        return pagoRepositorioJpa.save(pago);
    }
}
