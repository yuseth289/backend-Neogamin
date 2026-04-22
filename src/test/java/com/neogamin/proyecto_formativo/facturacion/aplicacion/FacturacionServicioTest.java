package com.neogamin.proyecto_formativo.facturacion.aplicacion;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.facturacion.dominio.Factura;
import com.neogamin.proyecto_formativo.facturacion.infraestructura.FacturaRepositorioJpa;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.FacturaEmitidaEmailEvent;
import com.neogamin.proyecto_formativo.pago.dominio.Pago;
import com.neogamin.proyecto_formativo.pago.dominio.TipoPago;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
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
class FacturacionServicioTest {

    @Mock
    private FacturaRepositorioJpa facturaRepositorioJpa;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private FacturacionServicio facturacionServicio;

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
}
