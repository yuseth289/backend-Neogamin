package com.neogamin.proyecto_formativo.carrito.aplicacion;

import com.neogamin.proyecto_formativo.carrito.api.dto.ActualizarCantidadCarritoRequest;
import com.neogamin.proyecto_formativo.carrito.api.dto.AgregarProductoCarritoRequest;
import com.neogamin.proyecto_formativo.carrito.api.dto.CarritoItemResponse;
import com.neogamin.proyecto_formativo.carrito.api.dto.CarritoResponse;
import com.neogamin.proyecto_formativo.carrito.api.dto.ResumenCarritoResponse;
import com.neogamin.proyecto_formativo.carrito.dominio.CarritoEntidad;
import com.neogamin.proyecto_formativo.carrito.dominio.CarritoItemEntidad;
import com.neogamin.proyecto_formativo.carrito.dominio.EstadoCarrito;
import com.neogamin.proyecto_formativo.carrito.infraestructura.CarritoItemRepositorioJpa;
import com.neogamin.proyecto_formativo.carrito.infraestructura.CarritoRepositorioJpa;
import com.neogamin.proyecto_formativo.catalogo.dominio.Producto;
import com.neogamin.proyecto_formativo.catalogo.infraestructura.ProductoRepositorioJpa;
import com.neogamin.proyecto_formativo.compartido.aplicacion.BadRequestException;
import com.neogamin.proyecto_formativo.compartido.aplicacion.NotFoundException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoItemResponse;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoResponse;
import com.neogamin.proyecto_formativo.pedido.dominio.EstadoPedido;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import com.neogamin.proyecto_formativo.pedido.dominio.PedidoDetalle;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoDetalleRepositorioJpa;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoServicio {

    private final CarritoRepositorioJpa carritoRepositorioJpa;
    private final CarritoItemRepositorioJpa carritoItemRepositorioJpa;
    private final ProductoRepositorioJpa productoRepositorioJpa;
    private final UsuarioRepositorioJpa usuarioRepositorioJpa;
    private final PedidoRepositorioJpa pedidoRepositorioJpa;
    private final PedidoDetalleRepositorioJpa pedidoDetalleRepositorioJpa;

    @Transactional
    public CarritoResponse obtenerMiCarrito() {
        return toResponse(obtenerOCrearCarritoActivo());
    }

    @Transactional
    public CarritoResponse agregarProducto(AgregarProductoCarritoRequest request) {
        validarCantidad(request.cantidad());
        var carrito = obtenerOCrearCarritoActivo();
        var producto = cargarProductoDisponible(request.productoId());
        validarMonedaCarrito(carrito, producto);

        var item = carritoItemRepositorioJpa.findByCarritoIdAndProductoId(carrito.getId(), producto.getId())
                .orElseGet(() -> {
                    var nuevo = new CarritoItemEntidad();
                    nuevo.setCarrito(carrito);
                    nuevo.setProducto(producto);
                    nuevo.setCantidad(0);
                    return nuevo;
                });

        item.setCantidad(item.getCantidad() + request.cantidad());
        carritoItemRepositorioJpa.save(item);
        return toResponse(recargarCarritoActivo(carrito.getUsuario().getId()));
    }

    @Transactional
    public CarritoResponse actualizarCantidad(Long idItem, ActualizarCantidadCarritoRequest request) {
        validarCantidad(request.cantidad());
        var carrito = obtenerOCrearCarritoActivo();
        var item = carritoItemRepositorioJpa.findByIdAndCarritoId(idItem, carrito.getId())
                .orElseThrow(() -> new NotFoundException("Item del carrito no encontrado"));

        item.setCantidad(request.cantidad());
        carritoItemRepositorioJpa.save(item);
        return toResponse(recargarCarritoActivo(carrito.getUsuario().getId()));
    }

    @Transactional
    public CarritoResponse eliminarItem(Long idItem) {
        var carrito = obtenerOCrearCarritoActivo();
        var item = carritoItemRepositorioJpa.findByIdAndCarritoId(idItem, carrito.getId())
                .orElseThrow(() -> new NotFoundException("Item del carrito no encontrado"));
        carritoItemRepositorioJpa.delete(item);
        return toResponse(recargarCarritoActivo(carrito.getUsuario().getId()));
    }

    @Transactional
    public CarritoResponse vaciarCarrito() {
        var carrito = obtenerOCrearCarritoActivo();
        carritoItemRepositorioJpa.deleteByCarritoId(carrito.getId());
        carrito.getItems().clear();
        return toResponse(carrito);
    }

    @Transactional
    public PedidoResponse convertirAPedido() {
        var carrito = obtenerOCrearCarritoActivo();
        if (carrito.getItems().isEmpty()) {
            throw new BadRequestException("No es posible convertir un carrito vacío en pedido");
        }

        var usuario = usuarioRepositorioJpa.findById(SeguridadUtils.usuarioId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        var pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setMoneda(resolverMonedaCarrito(carrito));
        pedido.setEstado(EstadoPedido.BORRADOR);
        pedido.setFechaCreacion(OffsetDateTime.now());
        pedido.setNeedsRecalc(false);
        pedido = pedidoRepositorioJpa.save(pedido);

        BigDecimal subtotal = BigDecimal.ZERO;
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
            subtotal = subtotal.add(subtotalLinea);
        }

        pedido.setSubtotalProductos(subtotal);
        pedido.setDescuento(BigDecimal.ZERO);
        pedido.setImpuesto(BigDecimal.ZERO);
        pedido.setCostoEnvio(BigDecimal.ZERO);
        pedido.setTotal(subtotal);
        pedido.setNeedsRecalc(false);
        pedido = pedidoRepositorioJpa.save(pedido);

        carrito.setEstado(EstadoCarrito.CONVERTIDO);
        carritoRepositorioJpa.save(carrito);
        carritoItemRepositorioJpa.deleteByCarritoId(carrito.getId());

        return toPedidoResponse(pedido);
    }

    private CarritoEntidad obtenerOCrearCarritoActivo() {
        var usuarioId = SeguridadUtils.usuarioId();
        return carritoRepositorioJpa.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseGet(() -> {
                    var usuario = usuarioRepositorioJpa.findById(usuarioId)
                            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
                    var carrito = new CarritoEntidad();
                    carrito.setUsuario(usuario);
                    carrito.setEstado(EstadoCarrito.ACTIVO);
                    return carritoRepositorioJpa.save(carrito);
                });
    }

    private CarritoEntidad recargarCarritoActivo(Long usuarioId) {
        return carritoRepositorioJpa.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new NotFoundException("Carrito no encontrado"));
    }

    private Producto cargarProductoDisponible(Long productoId) {
        var producto = productoRepositorioJpa.findById(productoId)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        if (producto.getDeletedAt() != null || producto.getEstado() != EstadoGenerico.ACTIVO) {
            throw new BadRequestException("No es posible operar con un producto inactivo o eliminado");
        }
        return producto;
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new BadRequestException("La cantidad debe ser mayor que cero");
        }
    }

    private void validarMonedaCarrito(CarritoEntidad carrito, Producto producto) {
        if (!carrito.getItems().isEmpty()) {
            var monedaCarrito = resolverMonedaCarrito(carrito);
            if (monedaCarrito != null && !monedaCarrito.equalsIgnoreCase(producto.getMoneda())) {
                throw new BadRequestException("No se pueden mezclar productos con distintas monedas en el mismo carrito");
            }
        }
    }

    private String resolverMonedaCarrito(CarritoEntidad carrito) {
        return carrito.getItems().stream()
                .map(item -> item.getProducto().getMoneda())
                .filter(moneda -> moneda != null && !moneda.isBlank())
                .findFirst()
                .orElse("COP");
    }

    private BigDecimal precioVigente(Producto producto) {
        return producto.getPrecioVigenteCache() != null ? producto.getPrecioVigenteCache() : producto.getPrecioLista();
    }

    private CarritoResponse toResponse(CarritoEntidad carrito) {
        var items = carrito.getItems().stream()
                .sorted(Comparator.comparing(CarritoItemEntidad::getId))
                .map(item -> {
                    var producto = item.getProducto();
                    var precioUnitario = precioVigente(producto);
                    return new CarritoItemResponse(
                            item.getId(),
                            producto.getId(),
                            producto.getSku(),
                            producto.getSlug(),
                            producto.getNombre(),
                            item.getCantidad(),
                            precioUnitario,
                            precioUnitario.multiply(BigDecimal.valueOf(item.getCantidad())),
                            producto.getMoneda()
                    );
                })
                .toList();

        var totalUnidades = items.stream()
                .map(CarritoItemResponse::cantidad)
                .reduce(0, Integer::sum);
        var subtotal = items.stream()
                .map(CarritoItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponse(
                carrito.getId(),
                carrito.getUsuario().getId(),
                carrito.getEstado().name(),
                items,
                new ResumenCarritoResponse(
                        items.size(),
                        totalUnidades,
                        subtotal,
                        subtotal,
                        items.isEmpty() ? "COP" : items.get(0).moneda()
                )
        );
    }

    private PedidoResponse toPedidoResponse(Pedido pedido) {
        List<PedidoItemResponse> items = pedido.getDetalles().stream()
                .map(item -> new PedidoItemResponse(
                        item.getProducto().getId(),
                        item.getProductoSku(),
                        item.getProductoNombre(),
                        item.getCantidad(),
                        item.getPrecioFinalUnitario(),
                        item.getTotalLinea()
                ))
                .toList();

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
                items
        );
    }
}
