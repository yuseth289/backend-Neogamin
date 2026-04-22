package com.neogamin.proyecto_formativo.usuario.aplicacion;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.compartido.seguridad.HashTokenServicio;
import com.neogamin.proyecto_formativo.compartido.seguridad.JwtService;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.UsuarioInicioSesionEmailEvent;
import com.neogamin.proyecto_formativo.notificacion.aplicacion.UsuarioRegistradoEmailEvent;
import com.neogamin.proyecto_formativo.usuario.api.dto.LoginRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.RegistroUsuarioRequest;
import com.neogamin.proyecto_formativo.usuario.dominio.RolUsuario;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import com.neogamin.proyecto_formativo.usuario.infraestructura.SesionRepositorioJpa;
import com.neogamin.proyecto_formativo.usuario.infraestructura.UsuarioRepositorioJpa;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AutenticacionServicio autenticacionServicio;

    @Test
    void shouldPublishWelcomeNotificationWhenUserRegisters() {
        var request = new RegistroUsuarioRequest("Laura", "laura@example.com", "secreto123", "3001234567");
        var usuarioGuardado = new Usuario();
        usuarioGuardado.setId(15L);
        usuarioGuardado.setNombre("Laura");
        usuarioGuardado.setEmail("laura@example.com");
        usuarioGuardado.setPasswordHash("encoded");
        usuarioGuardado.setRol(RolUsuario.CLIENTE);
        usuarioGuardado.setEstado(EstadoGenerico.ACTIVO);

        when(usuarioRepositorioJpa.findByEmailIgnoreCase("laura@example.com")).thenReturn(java.util.Optional.empty());
        when(passwordEncoder.encode("secreto123")).thenReturn("encoded");
        when(usuarioRepositorioJpa.save(org.mockito.ArgumentMatchers.any(Usuario.class))).thenReturn(usuarioGuardado);

        autenticacionServicio.registrar(request);

        verify(applicationEventPublisher).publishEvent(org.mockito.ArgumentMatchers.<Object>argThat(event ->
                event instanceof UsuarioRegistradoEmailEvent usuarioRegistradoEmailEvent
                        && usuarioRegistradoEmailEvent.usuarioId().equals(15L)
                        && usuarioRegistradoEmailEvent.email().equals("laura@example.com")
        ));
    }

    @Test
    void shouldPublishLoginConfirmationNotificationWhenUserLogsIn() {
        var usuario = new Usuario();
        usuario.setId(9L);
        usuario.setNombre("Laura");
        usuario.setEmail("laura@example.com");
        usuario.setPasswordHash("encoded");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoGenerico.ACTIVO);
        var servletRequest = org.mockito.Mockito.mock(HttpServletRequest.class);

        when(usuarioRepositorioJpa.findByEmailIgnoreCase("laura@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("secreto123", "encoded")).thenReturn(true);
        when(jwtService.generarToken(org.mockito.ArgumentMatchers.eq(usuario), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("jwt-token");
        when(hashTokenServicio.sha256(org.mockito.ArgumentMatchers.anyString())).thenReturn("hash-session");
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(servletRequest.getHeader("User-Agent")).thenReturn("JUnit");

        autenticacionServicio.login(new LoginRequest("laura@example.com", "secreto123"), servletRequest);

        verify(applicationEventPublisher).publishEvent(org.mockito.ArgumentMatchers.<Object>argThat(event ->
                event instanceof UsuarioInicioSesionEmailEvent usuarioInicioSesionEmailEvent
                        && usuarioInicioSesionEmailEvent.usuarioId().equals(9L)
                        && usuarioInicioSesionEmailEvent.email().equals("laura@example.com")
        ));
    }

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

    @Test
    void shouldIgnoreLogoutWhenTokenIsInvalid() {
        when(jwtService.extraerSessionId("jwt-token")).thenThrow(new JwtException("bad token"));

        autenticacionServicio.logout("Bearer jwt-token");

        verify(sesionRepositorioJpa, never()).revokeByTokenHash(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }
}
