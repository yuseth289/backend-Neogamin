package com.neogamin.proyecto_formativo.usuario.aplicacion;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.compartido.seguridad.HashTokenServicio;
import com.neogamin.proyecto_formativo.compartido.seguridad.JwtService;
import com.neogamin.proyecto_formativo.usuario.infraestructura.SesionRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AutenticacionServicioTest {

    @Mock
    private UsuarioRepositorioJpa usuarioRepositorioJpa;

    @Mock
    private SesionRepositorioJpa sesionRepositorioJpa;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private HashTokenServicio hashTokenServicio;

    @InjectMocks
    private AutenticacionServicio autenticacionServicio;

    @Test
    void shouldRevokeSessionByTokenHashOnLogout() {
        when(jwtService.extraerSessionId("jwt-token")).thenReturn("session-1");
        when(hashTokenServicio.sha256("session-1")).thenReturn("hashed-session");

        autenticacionServicio.logout("Bearer jwt-token");

        verify(sesionRepositorioJpa).revokeByTokenHash(org.mockito.ArgumentMatchers.eq("hashed-session"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldIgnoreLogoutWhenHeaderIsMissing() {
        autenticacionServicio.logout(null);

        verify(jwtService, never()).extraerSessionId(org.mockito.ArgumentMatchers.anyString());
        verify(sesionRepositorioJpa, never()).revokeByTokenHash(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }
}
