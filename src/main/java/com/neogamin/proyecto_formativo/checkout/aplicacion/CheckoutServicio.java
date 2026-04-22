package com.neogamin.proyecto_formativo.checkout.aplicacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neogamin.proyecto_formativo.carrito.dominio.CarritoEntidad;
import com.neogamin.proyecto_formativo.carrito.dominio.CarritoItemEntidad;
import com.neogamin.proyecto_formativo.carrito.dominio.EstadoCarrito;
import com.neogamin.proyecto_formativo.carrito.infraestructura.CarritoRepositorioJpa;
import com.neogamin.proyecto_formativo.catalogo.dominio.Producto;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorioJpa;
import com.neogamin.proyecto_formativo.checkout.api.dto.ConfirmacionPedidoResponse;
import com.neogamin.proyecto_formativo.checkout.api.dto.DireccionCheckoutRequest;
import com.neogamin.proyecto_formativo.checkout.api.dto.DireccionCheckoutResponse;
import com.neogamin.proyecto_formativo.checkout.api.dto.GuardarEnvioRequest;
import com.neogamin.proyecto_formativo.checkout.api.dto.IniciarCheckoutResponse;
import com.neogamin.proyecto_formativo.checkout.api.dto.ItemResumenCheckoutResponse;
import com.neogamin.proyecto_formativo.checkout.api.dto.MetodoPagoCheckoutRequest;
import com.neogamin.proyecto_formativo.checkout.api.dto.ProcesarPagoRequest;
import com.neogamin.proyecto_formativo.checkout.api.dto.ProcesarPagoResponse;
import com.neogamin.proyecto_formativo.checkout.api.dto.ResumenCheckoutResponse;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.facturacion.infraestructura.FacturaRepositorioJpa;
import com.neogamin.proyecto_formativo.inventario.aplicacion.InventarioServicio;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PedidoCreadoEmailEvent;
import com.neogamin.proyecto_formativo.pago.aplicacion.PagoServicio;
import com.neogamin.proyecto_formativo.pago.dominio.EstadoPago;
import com.neogamin.proyecto_formativo.pago.dominio.Pago;
import com.neogamin.proyecto_formativo.pago.dominio.TipoPago;
import com.neogamin.proyecto_formativo.pago.infraestructura.PagoRepositorioJpa;
import com.neogamin.proyecto_formativo.pedido.api.dto.CheckoutRequest;
import com.neogamin.proyecto_formativo.pedido.dominio.EstadoPedido;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import com.neogamin.proyecto_formativo.pedido.dominio.PedidoDetalle;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoDetalleRepositorioJpa;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.Direccion;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.DireccionRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutServicio {

    private static final BigDecimal TARIFA_ENVIO = new BigDecimal("15000");
    private static final BigDecimal UMBRAL_ENVIO_GRATIS = new BigDecimal("200000");
    private static final BigDecimal TASA_IMPUESTO = new BigDecimal("0.19");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CarritoRepositorioJpa carritoRepositorioJpa;
    private final ProductoRepositorioJpa productoRepositorioJpa;
    private final PedidoRepositorioJpa pedidoRepositorioJpa;
    private final PedidoDetalleRepositorioJpa pedidoDetalleRepositorioJpa;
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final DireccionRepositorioJpa direccionRepositorioJpa;
    private final PagoRepositorioJpa pagoRepositorioJpa;
    private final FacturaRepositorioJpa facturaRepositorioJpa;
    private final InventarioServicio inventarioServicio;
    private final PagoServicio pagoServicio;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public IniciarCheckoutResponse iniciarCheckout() {
        var usuarioId = SeguridadUtils.usuarioId();
        var carrito = cargarCarritoActivoConItems(usuarioId);
        validarCarrito(carrito);
        var pedido = construirOActualizarPedidoBorrador(usuarioId, carrito);
        return toIniciarCheckoutResponse(pedido);
    }

    @Transactional
    public IniciarCheckoutResponse guardarEnvio(GuardarEnvioRequest request) {
        var pedido = cargarPedidoPropio(request.pedidoId());
        validarPedidoEditable(pedido);

        var envio = resolverDireccionEnvio(request, pedido.getUsuario());
        var factura = resolverDireccionFactura(request, pedido.getUsuario(), envio);

        pedido.setDireccionEnvio(request.direccionEnvioId() == null ? null : cargarDireccionPropia(request.direccionEnvioId()));
        pedido.setDireccionFactura(request.direccionFacturaId() == null ? null : cargarDireccionPropia(request.direccionFacturaId()));
        pedido.setDireccionEnvioSnapshot(serializarDireccion(envio));
        pedido.setDireccionFacturaSnapshot(serializarDireccion(factura));
        recalcularMontosPedido(pedido);

        return toIniciarCheckoutResponse(pedido);
    }

    @Transactional
    public ProcesarPagoResponse procesarPago(ProcesarPagoRequest request) {
        var pedido = cargarPedidoPropio(request.pedidoId());
        validarPedidoParaPago(pedido);
        validarMetodoPago(request.metodoPago());

        if (pedido.getDireccionEnvioSnapshot() == null || pedido.getDireccionFacturaSnapshot() == null) {
            throw new BadRequestException("Debes guardar la información de envío y facturación antes de pagar");
        }

        var pagoExistente = pagoRepositorioJpa.findByPedidoId(pedido.getId()).orElse(null);
        if (pagoExistente == null) {
            inventarioServicio.reservarStock(pedido);
            pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        } else if (pagoExistente.getEstado() == EstadoPago.APROBADO || pagoExistente.getEstado() == EstadoPago.CAPTURADO) {
            throw new BadRequestException("El pedido ya tiene un pago aprobado");
        }

        var checkoutRequest = new CheckoutRequest(
                resolverProveedor(request.metodoPago().tipoPago()),
                "CHK-" + pedido.getNumeroPedido(),
                UUID.randomUUID().toString(),
                request.metodoPago().tipoPago()
        );

        var pago = pagoServicio.registrarPagoPendiente(pedido, checkoutRequest);
        pago.setPayloadRespuesta(serializarResultadoPago(request.metodoPago(), request.simularFallo()));

        var resultado = simularResultadoPago(request.metodoPago(), Boolean.TRUE.equals(request.simularFallo()));
        if (resultado == EstadoPago.APROBADO) {
            pagoServicio.aprobarPago(pago.getId());
            pedido = cargarPedidoPropio(pedido.getId());
            pedido.setFechaEstimadaEntrega(OffsetDateTime.now().plusDays(3));
            pago = pagoRepositorioJpa.findById(pago.getId()).orElseThrow(() -> new NotFoundException("Pago no encontrado"));
            marcarCarritoComoConvertido(pedido.getUsuario().getId());
        } else if (resultado == EstadoPago.RECHAZADO) {
            pagoServicio.rechazarPago(pago.getId());
            pedido = cargarPedidoPropio(pedido.getId());
            pago = pagoRepositorioJpa.findById(pago.getId()).orElseThrow(() -> new NotFoundException("Pago no encontrado"));
        } else {
            pedido.setFechaEstimadaEntrega(OffsetDateTime.now().plusDays(5));
            marcarCarritoComoConvertido(pedido.getUsuario().getId());
        }

        return new ProcesarPagoResponse(
                pedido.getId(),
                pago.getId(),
                pedido.getNumeroPedido(),
                pedido.getEstado().name(),
                pago.getEstado().name(),
                pago.getTipoPago().name(),
                mensajePorEstadoPago(pago.getEstado()),
                pedido.getTotal(),
                pedido.getFechaCreacion(),
                pedido.getFechaEstimadaEntrega()
        );
    }

    @Transactional(readOnly = true)
    public ConfirmacionPedidoResponse obtenerConfirmacion(String numeroPedido) {
        var pedido = pedidoRepositorioJpa.findWithDetallesByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        if (!pedido.getUsuario().getId().equals(SeguridadUtils.usuarioId())) {
            throw new ForbiddenException("No tienes acceso a este pedido");
        }

        var pago = pagoRepositorioJpa.findByPedidoId(pedido.getId()).orElse(null);
        var factura = facturaRepositorioJpa.findByPedidoId(pedido.getId()).orElse(null);
        var items = pedido.getDetalles().stream()
                .map(this::toItemResumen)
                .toList();

        return new ConfirmacionPedidoResponse(
                pago == null ? "Tu pedido fue creado" : mensajePorEstadoPago(pago.getEstado()),
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getEstado().name(),
                pago == null ? "SIN_PAGO" : pago.getEstado().name(),
                pago == null ? null : pago.getTipoPago().name(),
                pedido.getTotal(),
                items.stream().map(ItemResumenCheckoutResponse::cantidad).reduce(0, Integer::sum),
                pedido.getFechaCreacion(),
                pedido.getFechaEstimadaEntrega(),
                factura == null ? null : factura.getNumeroFactura(),
                deserializarDireccion(pedido.getDireccionEnvioSnapshot()),
                deserializarDireccion(pedido.getDireccionFacturaSnapshot()),
                resumenDesdePedido(pedido, items),
                items
        );
    }

    private Pedido construirOActualizarPedidoBorrador(Long usuarioId, CarritoEntidad carrito) {
        var usuario = usuarioRepositorioJpa.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        var pedido = pedidoRepositorioJpa.findTopByUsuarioIdAndEstadoOrderByFechaCreacionDesc(usuarioId, EstadoPedido.BORRADOR)
                .orElseGet(Pedido::new);
        var nuevoPedido = pedido.getId() == null;

        if (pedido.getId() == null) {
            pedido.setUsuario(usuario);
            pedido.setFechaCreacion(OffsetDateTime.now());
            pedido.setEstado(EstadoPedido.BORRADOR);
            pedido.setNumeroPedido(generarNumeroPedido(usuarioId));
        }

        pedido.setMoneda(resolverMonedaCarrito(carrito));
        pedido.setNeedsRecalc(false);
        pedido = pedidoRepositorioJpa.save(pedido);

        pedidoDetalleRepositorioJpa.deleteByPedidoId(pedido.getId());
        pedido.getDetalles().clear();

        for (var itemCarrito : carrito.getItems()) {
            var producto = cargarProductoDisponible(itemCarrito.getProducto().getId());
            var precioUnitario = precioVigente(producto);
            var subtotalLinea = precioUnitario.multiply(BigDecimal.valueOf(itemCarrito.getCantidad()));

            var detalle = new PedidoDetalle();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setProductoSku(producto.getSku());
            detalle.setProductoNombre(producto.getNombre());
            detalle.setMoneda(producto.getMoneda());
            detalle.setCantidad(itemCarrito.getCantidad());
            detalle.setPrecioListaUnitario(precioUnitario);
            detalle.setDescuentoUnitario(BigDecimal.ZERO);
            detalle.setPrecioFinalUnitario(precioUnitario);
            detalle.setImpuestoUnitario(BigDecimal.ZERO);
            detalle.setSubtotalLinea(subtotalLinea);
            detalle.setTotalLinea(subtotalLinea);
            pedidoDetalleRepositorioJpa.save(detalle);
            pedido.getDetalles().add(detalle);
        }

        recalcularMontosPedido(pedido);
        var pedidoGuardado = pedidoRepositorioJpa.save(pedido);
        if (nuevoPedido) {
            applicationEventPublisher.publishEvent(new PedidoCreadoEmailEvent(
                    pedidoGuardado.getId(),
                    pedidoGuardado.getNumeroPedido(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    pedidoGuardado.getTotal(),
                    pedidoGuardado.getMoneda()
            ));
        }
        return pedidoGuardado;
    }

    private void recalcularMontosPedido(Pedido pedido) {
        var subtotal = pedido.getDetalles().stream()
                .map(PedidoDetalle::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var impuesto = subtotal.multiply(TASA_IMPUESTO).setScale(2, RoundingMode.HALF_UP);
        var costoEnvio = subtotal.compareTo(UMBRAL_ENVIO_GRATIS) >= 0 || subtotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : TARIFA_ENVIO;

        pedido.setSubtotalProductos(subtotal);
        pedido.setDescuento(BigDecimal.ZERO);
        pedido.setImpuesto(impuesto);
        pedido.setCostoEnvio(costoEnvio);
        pedido.setTotal(subtotal.add(impuesto).add(costoEnvio));
        pedido.setNeedsRecalc(false);
    }

    private CarritoEntidad cargarCarritoActivoConItems(Long usuarioId) {
        return carritoRepositorioJpa.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new BadRequestException("No tienes un carrito activo"));
    }

    private void validarCarrito(CarritoEntidad carrito) {
        if (carrito.getItems().isEmpty()) {
            throw new BadRequestException("No es posible iniciar checkout con un carrito vacío");
        }
        carrito.getItems().forEach(item -> {
            if (item.getCantidad() == null || item.getCantidad() < 1) {
                throw new BadRequestException("El carrito tiene cantidades inválidas");
            }
            cargarProductoDisponible(item.getProducto().getId());
        });
    }

    private Producto cargarProductoDisponible(Long productoId) {
        var producto = productoRepositorioJpa.findById(productoId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        if (producto.getDeletedAt() != null || producto.getEstado() != EstadoGenerico.ACTIVO) {
            throw new BadRequestException("El producto " + producto.getNombre() + " no está disponible para checkout");
        }
        return producto;
    }

    private Pedido cargarPedidoPropio(Long pedidoId) {
        var pedido = pedidoRepositorioJpa.findWithDetallesById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
        if (!pedido.getUsuario().getId().equals(SeguridadUtils.usuarioId())) {
            throw new ForbiddenException("No tienes acceso a este pedido");
        }
        return pedido;
    }

    private void validarPedidoEditable(Pedido pedido) {
        if (pedido.getEstado() != EstadoPedido.BORRADOR) {
            throw new BadRequestException("Solo puedes editar envío de un pedido en borrador");
        }
        if (pedido.getDetalles().isEmpty()) {
            throw new BadRequestException("El pedido no tiene productos");
        }
    }

    private void validarPedidoParaPago(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) {
            throw new BadRequestException("El pedido no tiene productos para pagar");
        }
        if (pedido.getEstado() == EstadoPedido.PAGADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new BadRequestException("El pedido ya se encuentra pagado");
        }
    }

    private Direccion cargarDireccionPropia(Long direccionId) {
        var direccion = direccionRepositorioJpa.findByIdAndUsuarioId(direccionId, SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("Dirección no encontrada"));
        if (direccion.getDeletedAt() != null || direccion.getEstado() != EstadoGenerico.ACTIVO) {
            throw new BadRequestException("La dirección seleccionada no está disponible");
        }
        return direccion;
    }

    private DireccionCheckoutResponse resolverDireccionEnvio(GuardarEnvioRequest request, Usuario usuario) {
        if (request.direccionEnvioId() != null) {
            return snapshotDesdeDireccion(cargarDireccionPropia(request.direccionEnvioId()), usuario);
        }
        if (request.direccionEnvio() != null) {
            return toDireccionResponse(request.direccionEnvio());
        }
        throw new BadRequestException("Debes indicar una dirección de envío guardada o manual");
    }

    private DireccionCheckoutResponse resolverDireccionFactura(
            GuardarEnvioRequest request,
            Usuario usuario,
            DireccionCheckoutResponse envio
    ) {
        if (Boolean.TRUE.equals(request.mismaDireccionFacturacion())) {
            return envio;
        }
        if (request.direccionFacturaId() != null) {
            return snapshotDesdeDireccion(cargarDireccionPropia(request.direccionFacturaId()), usuario);
        }
        if (request.direccionFactura() != null) {
            return toDireccionResponse(request.direccionFactura());
        }
        return envio;
    }

    private DireccionCheckoutResponse snapshotDesdeDireccion(Direccion direccion, Usuario usuario) {
        return new DireccionCheckoutResponse(
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getCiudad(),
                direccion.getDepartamento(),
                direccion.getCodigoPostal(),
                direccion.getPais(),
                direccion.getReferencia()
        );
    }

    private DireccionCheckoutResponse toDireccionResponse(DireccionCheckoutRequest request) {
        return new DireccionCheckoutResponse(
                request.nombreCompleto(),
                request.correoElectronico(),
                request.telefono(),
                request.direccion(),
                request.apartamentoInterior(),
                request.ciudad(),
                request.estadoRegion(),
                request.codigoPostal(),
                request.pais(),
                request.referenciaEntrega()
        );
    }

    private IniciarCheckoutResponse toIniciarCheckoutResponse(Pedido pedido) {
        var items = pedido.getDetalles().stream()
                .map(this::toItemResumen)
                .toList();
        return new IniciarCheckoutResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getEstado().name(),
                resumenDesdePedido(pedido, items),
                items,
                deserializarDireccion(pedido.getDireccionEnvioSnapshot()),
                deserializarDireccion(pedido.getDireccionFacturaSnapshot())
        );
    }

    private ItemResumenCheckoutResponse toItemResumen(PedidoDetalle item) {
        return new ItemResumenCheckoutResponse(
                item.getProducto().getId(),
                item.getProductoSku(),
                item.getProductoNombre(),
                item.getCantidad(),
                item.getPrecioFinalUnitario(),
                item.getTotalLinea()
        );
    }

    private ResumenCheckoutResponse resumenDesdePedido(Pedido pedido, List<ItemResumenCheckoutResponse> items) {
        return new ResumenCheckoutResponse(
                items.stream().map(ItemResumenCheckoutResponse::cantidad).reduce(0, Integer::sum),
                pedido.getSubtotalProductos(),
                pedido.getImpuesto(),
                pedido.getCostoEnvio(),
                pedido.getTotal(),
                pedido.getMoneda()
        );
    }

    private String serializarDireccion(DireccionCheckoutResponse direccion) {
        try {
            return OBJECT_MAPPER.writeValueAsString(direccion);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("No fue posible guardar la dirección del checkout");
        }
    }

    private DireccionCheckoutResponse deserializarDireccion(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, DireccionCheckoutResponse.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("No fue posible leer la dirección del pedido");
        }
    }

    private String serializarResultadoPago(MetodoPagoCheckoutRequest metodoPago, Boolean simularFallo) {
        try {
            return OBJECT_MAPPER.writeValueAsString(java.util.Map.of(
                    "metodo", metodoPago.tipoPago().name(),
                    "simularFallo", Boolean.TRUE.equals(simularFallo),
                    "fecha", OffsetDateTime.now().toString()
            ));
        } catch (JsonProcessingException e) {
            throw new BadRequestException("No fue posible registrar el resultado del pago");
        }
    }

    private EstadoPago simularResultadoPago(MetodoPagoCheckoutRequest metodoPago, boolean simularFallo) {
        if (simularFallo) {
            return EstadoPago.RECHAZADO;
        }
        return switch (metodoPago.tipoPago()) {
            case TARJETA -> {
                var numeroTarjeta = metodoPago.numeroTarjeta();
                if (numeroTarjeta != null && numeroTarjeta.replaceAll("\\s+", "").endsWith("4242")) {
                    yield EstadoPago.APROBADO;
                }
                yield EstadoPago.RECHAZADO;
            }
            case NEQUI, PAYPAL -> EstadoPago.APROBADO;
            case EFECTY -> EstadoPago.PENDIENTE;
            default -> throw new BadRequestException("El método de pago seleccionado no está soportado en checkout");
        };
    }

    private String resolverProveedor(TipoPago tipoPago) {
        return switch (tipoPago) {
            case TARJETA -> "SIMULADOR_TARJETA";
            case PAYPAL -> "SIMULADOR_PAYPAL";
            case EFECTY -> "SIMULADOR_EFECTY";
            case NEQUI -> "SIMULADOR_NEQUI";
            default -> "SIMULADOR";
        };
    }

    private void validarMetodoPago(MetodoPagoCheckoutRequest metodoPago) {
        if (metodoPago.tipoPago() == TipoPago.TARJETA) {
            if (metodoPago.numeroTarjeta() == null || metodoPago.numeroTarjeta().isBlank()) {
                throw new BadRequestException("Debes indicar el número de tarjeta");
            }
            if (metodoPago.nombreTitular() == null || metodoPago.nombreTitular().isBlank()) {
                throw new BadRequestException("Debes indicar el nombre del titular");
            }
            if (metodoPago.fechaVencimiento() == null || metodoPago.fechaVencimiento().isBlank()) {
                throw new BadRequestException("Debes indicar la fecha de vencimiento");
            }
            if (metodoPago.cvv() == null || metodoPago.cvv().isBlank()) {
                throw new BadRequestException("Debes indicar el CVV");
            }
        }
    }

    private String mensajePorEstadoPago(EstadoPago estadoPago) {
        return switch (estadoPago) {
            case APROBADO, CAPTURADO -> "Gracias por tu compra. Tu pedido fue confirmado.";
            case PENDIENTE, AUTORIZADO -> "Tu pedido fue creado y está pendiente de confirmación de pago.";
            case RECHAZADO, ANULADO -> "El pago fue rechazado y el pedido no pudo confirmarse.";
            default -> "Estado de pago actualizado.";
        };
    }

    private String generarNumeroPedido(Long usuarioId) {
        return "PED-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + usuarioId;
    }

    private void marcarCarritoComoConvertido(Long usuarioId) {
        carritoRepositorioJpa.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO).ifPresent(carrito -> {
            carrito.setEstado(EstadoCarrito.CONVERTIDO);
            carrito.getItems().clear();
            carritoRepositorioJpa.save(carrito);
        });
    }

    private BigDecimal precioVigente(Producto producto) {
        return producto.getPrecioVigenteCache() != null ? producto.getPrecioVigenteCache() : producto.getPrecioLista();
    }

    private String resolverMonedaCarrito(CarritoEntidad carrito) {
        return carrito.getItems().stream()
                .map(CarritoItemEntidad::getProducto)
                .map(Producto::getMoneda)
                .findFirst()
                .orElse("COP");
    }
}
