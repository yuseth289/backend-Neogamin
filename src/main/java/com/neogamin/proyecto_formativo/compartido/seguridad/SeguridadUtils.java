package com.neogamin.proyecto_formativo.compartido.seguridad;

import com.neogamin.proyecto_formativo.compartido.aplicacion.UnauthorizedException;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SeguridadUtils {

    private SeguridadUtils() {
    }

    public static Usuario usuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario usuario)) {
            throw new UnauthorizedException("No hay un usuario autenticado");
        }
        return usuario;
    }

    public static Long usuarioId() {
        return usuarioAutenticado().getId();
    }
}
