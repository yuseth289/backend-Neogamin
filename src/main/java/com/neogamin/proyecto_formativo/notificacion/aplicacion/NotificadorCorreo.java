package com.neogamin.proyecto_formativo.notificacion.aplicacion;

import com.neogamin.proyecto_formativo.notificacion.dominio.MensajeCorreo;

public interface NotificadorCorreo {

    void enviar(MensajeCorreo mensajeCorreo);
}
