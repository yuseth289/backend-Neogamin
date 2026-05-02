package com.neogamin.proyecto_formativo.notificacion.infraestructura;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.notificacion.dominio.MensajeCorreo;
import com.neogamin.proyecto_formativo.notificacion.dominio.TipoNotificacionEmail;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class SmtpNotificadorCorreoTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Test
    void shouldNotPropagateMailFailures() {
        var smtpNotificadorCorreo = new SmtpNotificadorCorreo(
                javaMailSender,
                new NotificacionProperties(true, "neogaming2026@gmail.com")
        );
        var mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        var mensajeCorreo = new MensajeCorreo(
                TipoNotificacionEmail.PAGO_APROBADO,
                "neogaming2026@gmail.com",
                "cliente@example.com",
                "Pago aprobado",
                "<html><body>ok</body></html>"
        );

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("smtp down")).when(javaMailSender).send(mimeMessage);

        assertThatCode(() -> smtpNotificadorCorreo.enviar(mensajeCorreo))
                .doesNotThrowAnyException();

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void shouldSkipSendingWhenEmailNotificationsAreDisabled() {
        var smtpNotificadorCorreo = new SmtpNotificadorCorreo(
                javaMailSender,
                new NotificacionProperties(false, "neogaming2026@gmail.com")
        );
        var mensajeCorreo = new MensajeCorreo(
                TipoNotificacionEmail.PAGO_APROBADO,
                "neogaming2026@gmail.com",
                "cliente@example.com",
                "Pago aprobado",
                "<html><body>ok</body></html>"
        );

        assertThatCode(() -> smtpNotificadorCorreo.enviar(mensajeCorreo))
                .doesNotThrowAnyException();

        verify(javaMailSender, never()).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }
}
