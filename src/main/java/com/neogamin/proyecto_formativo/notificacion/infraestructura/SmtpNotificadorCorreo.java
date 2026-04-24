package com.neogamin.proyecto_formativo.notificacion.infraestructura;

import com.neogamin.proyecto_formativo.notificacion.aplicacion.NotificadorCorreo;
import com.neogamin.proyecto_formativo.notificacion.dominio.MensajeCorreo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpNotificadorCorreo implements NotificadorCorreo {

    private static final Logger logger = LoggerFactory.getLogger(SmtpNotificadorCorreo.class);

    private final JavaMailSender javaMailSender;
    private final NotificacionProperties notificacionProperties;

    @Override
    public void enviar(MensajeCorreo mensajeCorreo) {
        if (!notificacionProperties.emailHabilitado()) {
            logger.info(
                    "Envio de correo deshabilitado. Se omite la notificacion {} para {}",
                    mensajeCorreo.tipo(),
                    mensajeCorreo.destinatario()
            );
            return;
        }

        try {
            var mimeMessage = javaMailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(mensajeCorreo.remitente());
            helper.setTo(mensajeCorreo.destinatario());
            helper.setSubject(mensajeCorreo.asunto());
            helper.setText(mensajeCorreo.contenidoHtml(), true);
            javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException ex) {
            logger.error(
                    "Fallo el envio del correo transaccional tipo {} para {}",
                    mensajeCorreo.tipo(),
                    mensajeCorreo.destinatario(),
                    ex
            );
        }
    }
}
