package com.neogamin.proyecto_formativo.pago.aplicacion;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.neogamin.proyecto_formativo.pedido.infraestructura.PedidoRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Test
    void shouldPublishApprovedPaymentNotification() {
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

    private Pedido crearPedido() {
        var usuario = new Usuario();
        usuario.setId(2L);
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
}
