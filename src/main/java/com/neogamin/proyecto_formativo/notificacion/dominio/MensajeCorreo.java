package com.neogamin.proyecto_formativo.notificacion.dominio;

public record MensajeCorreo(
        TipoNotificacionEmail tipo,
        String remitente,
        String destinatario,
        String asunto,
        String contenidoHtml
) {
}
