package com.neogamin.proyecto_formativo.pedido.aplicacion;

import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorioJpa;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.inventario.aplicacion.InventarioServicio;
import com.neogamin.proyecto_formativo.pago.aplicacion.PagoServicio;
import com.neogamin.proyecto_formativo.pedido.api.dto.AgregarItemPedidoRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.CheckoutRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.CheckoutResponse;
import com.neogamin.proyecto_formativo.pedido.api.dto.CrearPedidoRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.FiltroMisPedidosRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoItemResponse;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoListadoResponse;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoResponse;
import com.neogamin.proyecto_formativo.pedido.dominio.EstadoPedido;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import com.neogamin.proyecto_formativo.pedido.dominio.PedidoDetalle;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoDetalleRepositorioJpa;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PedidoServicio {

    private final PedidoRepositorioJpa pedidoRepositorioJpa;
    private final PedidoDetalleRepositorioJpa pedidoDetalleRepositorioJpa;
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final ProductoRepositorioJpa productoRepositorioJpa;
    private final InventarioServicio inventarioServicio;
    private final PagoServicio pagoServicio;
    private final PedidoMapper pedidoMapper;

    @Transactional
    public PedidoResponse crearPedido(CrearPedidoRequest request) {
        var usuario = usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        var pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setMoneda(request.moneda() == null || request.moneda().isBlank() ? "COP" : request.moneda());
        pedido.setEstado(EstadoPedido.BORRADOR);
        pedido.setFechaCreacion(OffsetDateTime.now());
        pedido.setNeedsRecalc(false);
        return toResponse(pedidoRepositorioJpa.save(pedido));
    }

    @Transactional
    public PedidoResponse agregarItem(Long pedidoId, AgregarItemPedidoRequest request) {
        var pedido = cargarPedidoPropio(pedidoId);
        if (pedido.getEstado() != EstadoPedido.BORRADOR && pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new BadRequestException("El pedido no permite agregar ítems en el estado actual");
        }

        var producto = productoRepositorioJpa.findById(request.productoId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        var detalle = pedidoDetalleRepositorioJpa.findByPedidoIdAndProductoId(pedidoId, request.productoId())
                .orElseGet(PedidoDetalle::new);

        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setProductoSku(producto.getSku());
        detalle.setProductoNombre(producto.getNombre());
        detalle.setMoneda(producto.getMoneda());
        detalle.setCantidad(request.cantidad());
        detalle.setPrecioListaUnitario(producto.getPrecioVigenteCache() != null ? producto.getPrecioVigenteCache() : producto.getPrecioLista());
        detalle.setDescuentoUnitario(request.descuentoUnitario() == null ? BigDecimal.ZERO : request.descuentoUnitario());
        detalle.setImpuestoUnitario(request.impuestoUnitario() == null ? BigDecimal.ZERO : request.impuestoUnitario());
        detalle.setPrecioFinalUnitario(detalle.getPrecioListaUnitario().subtract(detalle.getDescuentoUnitario()).max(BigDecimal.ZERO));
        detalle.setSubtotalLinea(detalle.getPrecioFinalUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())));
        detalle.setTotalLinea(detalle.getPrecioFinalUnitario().add(detalle.getImpuestoUnitario()).multiply(BigDecimal.valueOf(detalle.getCantidad())));
        pedidoDetalleRepositorioJpa.save(detalle);

        pedido.setNeedsRecalc(true);
        return recalcularPedido(pedidoId);
    }

    @Transactional
    public PedidoResponse recalcularPedido(Long pedidoId) {
        var pedido = cargarPedidoPropio(pedidoId);
        var subtotal = pedido.getDetalles().stream()
                .map(PedidoDetalle::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var impuesto = pedido.getDetalles().stream()
                .map(item -> item.getImpuestoUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setSubtotalProductos(subtotal);
        pedido.setImpuesto(impuesto);
        pedido.setTotal(subtotal.subtract(pedido.getDescuento()).max(BigDecimal.ZERO).add(impuesto).add(pedido.getCostoEnvio()));
        pedido.setNeedsRecalc(false);
        return toResponse(pedido);
    }

    @Transactional
    public CheckoutResponse checkout(Long pedidoId, CheckoutRequest request) {
        var pedido = cargarPedidoPropio(pedidoId);
        if (pedido.getDetalles().isEmpty()) {
            throw new BadRequestException("No es posible hacer checkout de un pedido vacío");
        }
        if (Boolean.TRUE.equals(pedido.getNeedsRecalc())) {
            recalcularPedido(pedidoId);
            pedido = cargarPedidoPropio(pedidoId);
        }

        inventarioServicio.reservarStock(pedido);
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        var pago = pagoServicio.registrarPagoPendiente(pedido, request);
        return new CheckoutResponse(pedido.getId(), pago.getId(), pedido.getEstado().name(), pago.getEstado().name());
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long pedidoId) {
        return toResponse(cargarPedidoPropio(pedidoId));
    }

    @Transactional(readOnly = true)
    public Page<PedidoListadoResponse> listarMisPedidos(FiltroMisPedidosRequest filtro) {
        var usuarioId = SeguridadUtils.usuarioId();
        var pageable = crearPageable(filtro);
        var estado = parsearEstado(filtro.getEstado());

        Page<Pedido> pagina = estado == null
                ? pedidoRepositorioJpa.findByUsuarioId(usuarioId, pageable)
                : pedidoRepositorioJpa.findByUsuarioIdAndEstado(usuarioId, estado, pageable);

        return pagina.map(pedidoMapper::toListadoResponse);
    }

    private Pedido cargarPedidoPropio(Long pedidoId) {
        var pedido = pedidoRepositorioJpa.findWithDetallesById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        if (!pedido.getUsuario().getId().equals(SeguridadUtils.usuarioId())) {
            throw new ForbiddenException("No tienes acceso a este pedido");
        }
        return pedido;
    }

    private PageRequest crearPageable(FiltroMisPedidosRequest filtro) {
        var pagina = filtro.getPage() == null ? 0 : filtro.getPage();
        var tamano = filtro.getSize() == null ? 10 : filtro.getSize();
        if (pagina < 0) {
            throw new BadRequestException("La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > 100) {
            throw new BadRequestException("El tamaño de página debe estar entre 1 y 100");
        }
        return PageRequest.of(pagina, tamano, Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    }

    private EstadoPedido parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        try {
            return EstadoPedido.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("El estado indicado no es válido");
        }
    }

    private PedidoResponse toResponse(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(),
                pedido.getUsuario().getId(),
                pedido.getEstado().name(),
                pedido.getMoneda(),
                pedido.getSubtotalProductos(),
                pedido.getImpuesto(),
                pedido.getCostoEnvio(),
                pedido.getTotal(),
                Boolean.TRUE.equals(pedido.getNeedsRecalc()),
                pedido.getDetalles().stream()
                        .map(item -> new PedidoItemResponse(
                                item.getProducto().getId(),
                                item.getProductoSku(),
                                item.getProductoNombre(),
                                item.getCantidad(),
                                item.getPrecioFinalUnitario(),
                                item.getTotalLinea()
                        ))
                        .toList()
        );
    }
}
