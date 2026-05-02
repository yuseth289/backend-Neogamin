package com.neogamin.proyecto_formativo.facturacion.aplicacion;

import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.facturacion.api.dto.FacturaResponse;
import com.neogamin.proyecto_formativo.facturacion.dominio.EstadoFactura;
import com.neogamin.proyecto_formativo.facturacion.dominio.Factura;
import com.neogamin.proyecto_formativo.facturacion.infraestructura.FacturaRepositorioJpa;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.FacturaEmitidaEmailEvent;
import com.neogamin.proyecto_formativo.pago.dominio.Pago;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FacturacionServicio {

    private final FacturaRepositorioJpa facturaRepositorioJpa;
    private final PedidoRepositorioJpa pedidoRepositorioJpa;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Factura emitir(Pedido pedido, Pago pago) {
        return facturaRepositorioJpa.findByPedidoId(pedido.getId())
                .orElseGet(() -> crearFactura(pedido, pago));
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtenerPorPedido(Long pedidoId) {
        var factura = facturaRepositorioJpa.findByPedidoId(pedidoId)
                .orElseThrow(() -> new NotFoundException("Factura no encontrada"));
        var pedido = pedidoRepositorioJpa.findWithDetallesById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        validarAccesoFactura(pedido);
        return toResponse(factura);
    }

    private void validarAccesoFactura(Pedido pedido) {
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
        throw new ForbiddenException("No tienes permisos para acceder a esta factura");
    }

    private boolean pedidoContieneProductoDelVendedor(Pedido pedido, Long vendedorId) {
        return pedido.getDetalles().stream()
                .anyMatch(detalle -> detalle.getProducto() != null
                        && detalle.getProducto().getVendedor() != null
                        && vendedorId.equals(detalle.getProducto().getVendedor().getId()));
    }

    private Factura crearFactura(Pedido pedido, Pago pago) {
        var factura = new Factura();
        factura.setPedido(pedido);
        factura.setPago(pago);
        factura.setNumeroFactura("FAC-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        factura.setMoneda(pedido.getMoneda());
        factura.setSubtotal(pedido.getSubtotalProductos());
        factura.setDescuento(pedido.getDescuento());
        factura.setImpuesto(pedido.getImpuesto());
        factura.setCostoEnvio(pedido.getCostoEnvio());
        factura.setTotalNeto(pedido.getTotal());
        factura.setMetodoPago(pago.getTipoPago());
        factura.setFechaEmision(OffsetDateTime.now());
        factura.setEstadoFactura(EstadoFactura.EMITIDA);
        var facturaGuardada = facturaRepositorioJpa.save(factura);
        applicationEventPublisher.publishEvent(new FacturaEmitidaEmailEvent(
                facturaGuardada.getId(),
                pedido.getId(),
                facturaGuardada.getNumeroFactura(),
                pedido.getNumeroPedido(),
                pedido.getUsuario().getNombre(),
                pedido.getUsuario().getEmail(),
                facturaGuardada.getTotalNeto(),
                facturaGuardada.getMoneda()
        ));
        return facturaGuardada;
    }

    public FacturaResponse toResponse(Factura factura) {
        return new FacturaResponse(
                factura.getId(),
                factura.getPedido().getId(),
                factura.getNumeroFactura(),
                factura.getEstadoFactura().name(),
                factura.getSubtotal(),
                factura.getImpuesto(),
                factura.getCostoEnvio(),
                factura.getTotalNeto(),
                factura.getMoneda()
        );
    }
}
