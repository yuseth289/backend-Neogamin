package com.neogamin.proyecto_formativo.pago.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.catalogo.dominio.Producto;
import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.facturacion.aplicacion.FacturacionServicio;
import com.neogamin.proyecto_formativo.inventario.aplicacion.InventarioServicio;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PagoAprobadoEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PagoRechazadoEmailEvent;
import com.neogamin.proyecto_formativo.pago.dominio.EstadoPago;
import com.neogamin.proyecto_formativo.pago.dominio.Pago;
import com.neogamin.proyecto_formativo.pago.dominio.TipoPago;
import com.neogamin.proyecto_formativo.pago.infraestructura.PagoRepositorioJpa;
import com.neogamin.proyecto_formativo.pedido.dominio.EstadoPedido;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import com.neogamin.proyecto_formativo.pedido.dominio.PedidoDetalle;
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PagoServicioTest {

    @Mock
    private PagoRepositorioJpa pagoRepositorioJpa;

    @Mock
    private PedidoRepositorioJpa pedidoRepositorioJpa;

    @Mock
    private InventarioServicio inventarioServicio;

    @Mock
    private FacturacionServicio facturacionServicio;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PagoServicio pagoServicio;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPublishApprovedPaymentNotification() {
        autenticar(usuario(1L, RolUsuario.ADMIN));
        var pedido = crearPedido();
        var pago = crearPago(pedido, EstadoPago.PENDIENTE);

        when(pagoRepositorioJpa.findById(5L)).thenReturn(Optional.of(pago));
        when(pedidoRepositorioJpa.findWithDetallesById(10L)).thenReturn(Optional.of(pedido));

        pagoServicio.aprobarPago(5L);

        verify(inventarioServicio).confirmarStock(pedido);
        verify(facturacionServicio).emitir(pedido, pago);
        verify(applicationEventPublisher).publishEvent(org.mockito.ArgumentMatchers.<Object>argThat(event ->
                event instanceof PagoAprobadoEmailEvent pagoAprobadoEmailEvent
                        && pagoAprobadoEmailEvent.numeroPedido().equals("PED-10")
                        && pagoAprobadoEmailEvent.emailUsuario().equals("cliente@example.com")
        ));
    }

    @Test
    void shouldPublishRejectedPaymentNotification() {
        autenticar(usuario(1L, RolUsuario.ADMIN));
        var pedido = crearPedido();
        var pago = crearPago(pedido, EstadoPago.PENDIENTE);

        when(pagoRepositorioJpa.findById(5L)).thenReturn(Optional.of(pago));
        when(pedidoRepositorioJpa.findWithDetallesById(10L)).thenReturn(Optional.of(pedido));

        pagoServicio.rechazarPago(5L);

        verify(inventarioServicio).liberarStock(pedido);
        verify(applicationEventPublisher).publishEvent(org.mockito.ArgumentMatchers.<Object>argThat(event ->
                event instanceof PagoRechazadoEmailEvent pagoRechazadoEmailEvent
                        && pagoRechazadoEmailEvent.numeroPedido().equals("PED-10")
                        && pagoRechazadoEmailEvent.emailUsuario().equals("cliente@example.com")
        ));
    }

    @Test
    void usuarioDuenioPuedeConsultarSuPago() {
        var pedido = crearPedido();
        var pago = crearPago(pedido, EstadoPago.PENDIENTE);
        autenticar(pedido.getUsuario());

        when(pagoRepositorioJpa.findById(5L)).thenReturn(Optional.of(pago));
        when(pedidoRepositorioJpa.findWithDetallesById(10L)).thenReturn(Optional.of(pedido));

        var response = pagoServicio.obtener(5L);

        assertThat(response.pedidoId()).isEqualTo(10L);
        assertThat(response.estado()).isEqualTo(EstadoPago.PENDIENTE.name());
    }

    @Test
    void usuarioAjenoNoPuedeConsultarPago() {
        var pedido = crearPedido();
        var pago = crearPago(pedido, EstadoPago.PENDIENTE);
        autenticar(usuario(99L, RolUsuario.CLIENTE));

        when(pagoRepositorioJpa.findById(5L)).thenReturn(Optional.of(pago));
        when(pedidoRepositorioJpa.findWithDetallesById(10L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pagoServicio.obtener(5L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void vendedorAjenoNoPuedeAprobarPago() {
        var pedido = crearPedidoConProductoVendedor(usuario(2L, RolUsuario.VENDEDOR));
        var pago = crearPago(pedido, EstadoPago.PENDIENTE);
        autenticar(usuario(99L, RolUsuario.VENDEDOR));

        when(pagoRepositorioJpa.findById(5L)).thenReturn(Optional.of(pago));
        when(pedidoRepositorioJpa.findWithDetallesById(10L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pagoServicio.aprobarPago(5L))
                .isInstanceOf(ForbiddenException.class);

        verify(inventarioServicio, never()).confirmarStock(pedido);
        verify(facturacionServicio, never()).emitir(pedido, pago);
    }

    @Test
    void clienteDuenioNoPuedeAprobarSuPropioPago() {
        var pedido = crearPedido();
        var pago = crearPago(pedido, EstadoPago.PENDIENTE);
        autenticar(pedido.getUsuario());

        when(pagoRepositorioJpa.findById(5L)).thenReturn(Optional.of(pago));
        when(pedidoRepositorioJpa.findWithDetallesById(10L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pagoServicio.aprobarPago(5L))
                .isInstanceOf(ForbiddenException.class);

        verify(inventarioServicio, never()).confirmarStock(pedido);
        verify(facturacionServicio, never()).emitir(pedido, pago);
    }

    private Pedido crearPedido() {
        var usuario = usuario(2L, RolUsuario.CLIENTE);
        usuario.setNombre("Cliente");
        usuario.setEmail("cliente@example.com");

        var pedido = new Pedido();
        pedido.setId(10L);
        pedido.setNumeroPedido("PED-10");
        pedido.setUsuario(usuario);
        pedido.setEstado(EstadoPedido.PENDIENTE_PAGO);
        pedido.setMoneda("COP");
        pedido.setTotal(new BigDecimal("199000"));
        return pedido;
    }

    private Pedido crearPedidoConProductoVendedor(Usuario vendedor) {
        var pedido = crearPedido();
        var producto = new Producto();
        producto.setId(15L);
        producto.setVendedor(vendedor);

        var detalle = new PedidoDetalle();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        pedido.getDetalles().add(detalle);
        return pedido;
    }

    private Pago crearPago(Pedido pedido, EstadoPago estadoPago) {
        var pago = new Pago();
        pago.setId(5L);
        pago.setPedido(pedido);
        pago.setUsuario(pedido.getUsuario());
        pago.setEstado(estadoPago);
        pago.setMonto(new BigDecimal("199000"));
        pago.setMoneda("COP");
        pago.setTipoPago(TipoPago.TARJETA);
        return pago;
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities())
        );
    }

    private Usuario usuario(Long id, RolUsuario rol) {
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Usuario " + id);
        usuario.setEmail("usuario" + id + "@example.com");
        usuario.setPasswordHash("hash");
        usuario.setRol(rol);
        usuario.setEstado(EstadoGenerico.ACTIVO);
        return usuario;
    }
}
