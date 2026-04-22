package com.neogamin.proyecto_formativo.notificacion.aplicacion;

import com.neogamin.proyecto_formativo.notificacion.dominio.MensajeCorreo;
import com.neogamin.proyecto_formativo.notificacion.dominio.TipoNotificacionEmail;
import com.neogamin.proyecto_formativo.notificacion.infraestructura.NotificacionProperties;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacionEmailTransaccionalServicio {

    private static final Locale LOCALE_COLOMBIA = Locale.of("es", "CO");

    private final NotificadorCorreo notificadorCorreo;
    private final NotificacionProperties notificacionProperties;

    public void enviarBienvenida(UsuarioRegistradoEmailEvent event) {
        notificadorCorreo.enviar(new MensajeCorreo(
                TipoNotificacionEmail.BIENVENIDA_USUARIO,
                notificacionProperties.emailRemitente(),
                event.email(),
                "Bienvenido a NeoGaming",
                """
                        <html>
                        <body>
                          <h2>Hola %s, bienvenido a NeoGaming</h2>
                          <p>Tu cuenta fue creada correctamente y ya puedes explorar el catalogo, guardar productos y completar compras.</p>
                          <p>Este es un correo transaccional relacionado con tu registro.</p>
                        </body>
                        </html>
                        """.formatted(nombreSeguro(event.nombre()))
        ));
    }

    public void enviarConfirmacionInicioSesion(UsuarioInicioSesionEmailEvent event) {
        notificadorCorreo.enviar(new MensajeCorreo(
                TipoNotificacionEmail.INICIO_SESION_CONFIRMADO,
                notificacionProperties.emailRemitente(),
                event.email(),
                "Confirmacion de inicio de sesion en NeoGaming",
                """
                        <html>
                        <body>
                          <h2>Hola %s, detectamos un inicio de sesion</h2>
                          <p>Tu cuenta accedio correctamente a NeoGaming.</p>
                          <p>Si fuiste tu, no necesitas hacer nada.</p>
                          <p>Si no reconoces este acceso, cambia tu contraseña y revisa tus sesiones activas.</p>
                        </body>
                        </html>
                        """.formatted(nombreSeguro(event.nombre()))
        ));
    }

    public void enviarPedidoCreado(PedidoCreadoEmailEvent event) {
        notificadorCorreo.enviar(new MensajeCorreo(
                TipoNotificacionEmail.PEDIDO_CREADO,
                notificacionProperties.emailRemitente(),
                event.emailUsuario(),
                "Recibimos tu pedido " + event.numeroPedido(),
                """
                        <html>
                        <body>
                          <h2>Hola %s, tu pedido fue creado</h2>
                          <p>Numero de pedido: <strong>%s</strong></p>
                          <p>Total: <strong>%s %s</strong></p>
                          <p>Te notificaremos cuando el estado del pago cambie.</p>
                        </body>
                        </html>
                        """.formatted(
                        nombreSeguro(event.nombreUsuario()),
                        event.numeroPedido(),
                        formatearMonto(event.total()),
                        event.moneda()
                )
        ));
    }

    public void enviarPagoAprobado(PagoAprobadoEmailEvent event) {
        notificadorCorreo.enviar(new MensajeCorreo(
                TipoNotificacionEmail.PAGO_APROBADO,
                notificacionProperties.emailRemitente(),
                event.emailUsuario(),
                "Pago aprobado para tu pedido " + event.numeroPedido(),
                """
                        <html>
                        <body>
                          <h2>Hola %s, tu pago fue aprobado</h2>
                          <p>Pedido: <strong>%s</strong></p>
                          <p>Monto aprobado: <strong>%s %s</strong></p>
                          <p>Metodo de pago: <strong>%s</strong></p>
                          <p>Tu compra ya esta siendo procesada.</p>
                        </body>
                        </html>
                        """.formatted(
                        nombreSeguro(event.nombreUsuario()),
                        event.numeroPedido(),
                        formatearMonto(event.monto()),
                        event.moneda(),
                        event.tipoPago()
                )
        ));
    }

    public void enviarPagoRechazado(PagoRechazadoEmailEvent event) {
        notificadorCorreo.enviar(new MensajeCorreo(
                TipoNotificacionEmail.PAGO_RECHAZADO,
                notificacionProperties.emailRemitente(),
                event.emailUsuario(),
                "Pago rechazado para tu pedido " + event.numeroPedido(),
                """
                        <html>
                        <body>
                          <h2>Hola %s, no pudimos aprobar tu pago</h2>
                          <p>Pedido: <strong>%s</strong></p>
                          <p>Monto intentado: <strong>%s %s</strong></p>
                          <p>Metodo de pago: <strong>%s</strong></p>
                          <p>Puedes intentar nuevamente con otro metodo de pago.</p>
                        </body>
                        </html>
                        """.formatted(
                        nombreSeguro(event.nombreUsuario()),
                        event.numeroPedido(),
                        formatearMonto(event.monto()),
                        event.moneda(),
                        event.tipoPago()
                )
        ));
    }

    public void enviarFacturaEmitida(FacturaEmitidaEmailEvent event) {
        notificadorCorreo.enviar(new MensajeCorreo(
                TipoNotificacionEmail.FACTURA_GENERADA,
                notificacionProperties.emailRemitente(),
                event.emailUsuario(),
                "Factura generada " + event.numeroFactura(),
                """
                        <html>
                        <body>
                          <h2>Hola %s, tu factura ya fue generada</h2>
                          <p>Factura: <strong>%s</strong></p>
                          <p>Pedido: <strong>%s</strong></p>
                          <p>Total facturado: <strong>%s %s</strong></p>
                          <p>Este correo corresponde a un comprobante transaccional de tu compra.</p>
                        </body>
                        </html>
                        """.formatted(
                        nombreSeguro(event.nombreUsuario()),
                        event.numeroFactura(),
                        event.numeroPedido(),
                        formatearMonto(event.total()),
                        event.moneda()
                )
        ));
    }

    private String nombreSeguro(String nombre) {
        return nombre == null || nombre.isBlank() ? "cliente" : nombre.trim();
    }

    private String formatearMonto(BigDecimal monto) {
        return NumberFormat.getNumberInstance(LOCALE_COLOMBIA).format(monto);
    }
}
