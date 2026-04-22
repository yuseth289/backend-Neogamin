package com.neogamin.proyecto_formativo.notificacion.api;

import com.neogamin.proyecto_formativo.notificacion.aplicacion.FacturaEmitidaEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.NotificacionEmailTransaccionalServicio;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PagoAprobadoEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PagoRechazadoEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.PedidoCreadoEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.UsuarioInicioSesionEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.UsuarioRegistradoEmailEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificacionEmailEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionEmailEventListener.class);

    private final NotificacionEmailTransaccionalServicio notificacionEmailTransaccionalServicio;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarUsuarioRegistrado(UsuarioRegistradoEmailEvent event) {
        ejecutarSeguro(
                "BIENVENIDA_USUARIO",
                event.email(),
                () -> notificacionEmailTransaccionalServicio.enviarBienvenida(event)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarInicioSesionConfirmado(UsuarioInicioSesionEmailEvent event) {
        ejecutarSeguro(
                "INICIO_SESION_CONFIRMADO",
                event.email(),
                () -> notificacionEmailTransaccionalServicio.enviarConfirmacionInicioSesion(event)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarPedidoCreado(PedidoCreadoEmailEvent event) {
        ejecutarSeguro(
                "PEDIDO_CREADO",
                event.emailUsuario(),
                () -> notificacionEmailTransaccionalServicio.enviarPedidoCreado(event)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarPagoAprobado(PagoAprobadoEmailEvent event) {
        ejecutarSeguro(
                "PAGO_APROBADO",
                event.emailUsuario(),
                () -> notificacionEmailTransaccionalServicio.enviarPagoAprobado(event)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarPagoRechazado(PagoRechazadoEmailEvent event) {
        ejecutarSeguro(
                "PAGO_RECHAZADO",
                event.emailUsuario(),
                () -> notificacionEmailTransaccionalServicio.enviarPagoRechazado(event)
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void manejarFacturaEmitida(FacturaEmitidaEmailEvent event) {
        ejecutarSeguro(
                "FACTURA_GENERADA",
                event.emailUsuario(),
                () -> notificacionEmailTransaccionalServicio.enviarFacturaEmitida(event)
        );
    }

    private void ejecutarSeguro(String tipo, String destinatario, Runnable accion) {
        try {
            accion.run();
        } catch (RuntimeException ex) {
            logger.error(
                    "Ocurrio un error inesperado procesando la notificacion {} para {}",
                    tipo,
                    destinatario,
                    ex
            );
        }
    }
}
