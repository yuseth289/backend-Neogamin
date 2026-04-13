package com.neogamin.proyecto_formativo.usuario.aplicacion;

import com.neogamin.proyecto_formativo.compartido.aplicacion.UnauthorizedException;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetallesServicio implements UserDetailsService {

    private final UsuarioRepositorioJpa usuarioRepositorioJpa;

    @Override
    public Usuario loadUserByUsername(String username) {
        return usuarioRepositorioJpa.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));
    }
}
