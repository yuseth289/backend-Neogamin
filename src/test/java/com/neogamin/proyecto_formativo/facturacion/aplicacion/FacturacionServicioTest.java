package com.neogamin.proyecto_formativo.facturacion.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.compartido.aplicacion.ForbiddenException;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.facturacion.dominio.EstadoFactura;
import com.neogamin.proyecto_formativo.facturacion.dominio.Factura;
import com.neogamin.proyecto_formativo.facturacion.infraestructura.FacturaRepositorioJpa;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.FacturaEmitidaEmailEvent;
import com.neogamin.proyecto_formativo.pago.dominio.Pago;
import com.neogamin.proyecto_formativo.pago.dominio.TipoPago;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
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
class FacturacionServicioTest {

    @Mock
    private FacturaRepositorioJpa facturaRepositorioJpa;

    @Mock
    private PedidoRepositorioJpa pedidoRepositorioJpa;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private FacturacionServicio facturacionServicio;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPublishInvoiceNotificationWhenInvoiceIsCreated() {
        var pedido = new Pedido();
        var usuario = new Usuario();
        usuario.setNombre("Cliente");
        usuario.setEmail("cliente@example.com");
        pedido.setId(8L);
        pedido.setNumeroPedido("PED-8");
        pedido.setUsuario(usuario);
        pedido.setMoneda("COP");
        pedido.setSubtotalProductos(new BigDecimal("100000"));
        pedido.setDescuento(BigDecimal.ZERO);
        pedido.setImpuesto(new BigDecimal("19000"));
        pedido.setCostoEnvio(new BigDecimal("15000"));
        pedido.setTotal(new BigDecimal("134000"));

        var pago = new Pago();
        pago.setTipoPago(TipoPago.TARJETA);

        when(facturaRepositorioJpa.findByPedidoId(8L)).thenReturn(Optional.empty());
        when(facturaRepositorioJpa.save(org.mockito.ArgumentMatchers.any(Factura.class))).thenAnswer(invocation -> {
            var factura = invocation.getArgument(0, Factura.class);
            factura.setId(20L);
            return factura;
        });

        facturacionServicio.emitir(pedido, pago);

        verify(applicationEventPublisher).publishEvent(org.mockito.ArgumentMatchers.<Object>argThat(event ->
                event instanceof FacturaEmitidaEmailEvent facturaEmitidaEmailEvent
                        && facturaEmitidaEmailEvent.numeroPedido().equals("PED-8")
                        && facturaEmitidaEmailEvent.emailUsuario().equals("cliente@example.com")
        ));
    }

    @Test
    void usuarioDuenioPuedeConsultarSuFactura() {
        var pedido = crearPedidoFacturado(usuario(2L, RolUsuario.CLIENTE));
        var factura = crearFactura(pedido);
        autenticar(pedido.getUsuario());

        when(facturaRepositorioJpa.findByPedidoId(8L)).thenReturn(Optional.of(factura));
        when(pedidoRepositorioJpa.findWithDetallesById(8L)).thenReturn(Optional.of(pedido));

        var response = facturacionServicio.obtenerPorPedido(8L);

        assertThat(response.pedidoId()).isEqualTo(8L);
        assertThat(response.numeroFactura()).isEqualTo("FAC-8");
    }

    @Test
    void usuarioAjenoNoPuedeConsultarFactura() {
        var pedido = crearPedidoFacturado(usuario(2L, RolUsuario.CLIENTE));
        var factura = crearFactura(pedido);
        autenticar(usuario(99L, RolUsuario.CLIENTE));

        when(facturaRepositorioJpa.findByPedidoId(8L)).thenReturn(Optional.of(factura));
        when(pedidoRepositorioJpa.findWithDetallesById(8L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> facturacionServicio.obtenerPorPedido(8L))
                .isInstanceOf(ForbiddenException.class);
    }

    private Pedido crearPedidoFacturado(Usuario usuario) {
        var pedido = new Pedido();
        pedido.setId(8L);
        pedido.setNumeroPedido("PED-8");
        pedido.setUsuario(usuario);
        pedido.setMoneda("COP");
        pedido.setSubtotalProductos(new BigDecimal("100000"));
        pedido.setDescuento(BigDecimal.ZERO);
        pedido.setImpuesto(new BigDecimal("19000"));
        pedido.setCostoEnvio(new BigDecimal("15000"));
        pedido.setTotal(new BigDecimal("134000"));
        return pedido;
    }

    private Factura crearFactura(Pedido pedido) {
        var factura = new Factura();
        factura.setId(20L);
        factura.setPedido(pedido);
        factura.setNumeroFactura("FAC-8");
        factura.setMoneda("COP");
        factura.setSubtotal(new BigDecimal("100000"));
        factura.setImpuesto(new BigDecimal("19000"));
        factura.setCostoEnvio(new BigDecimal("15000"));
        factura.setTotalNeto(new BigDecimal("134000"));
        factura.setEstadoFactura(EstadoFactura.EMITIDA);
        return factura;
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
