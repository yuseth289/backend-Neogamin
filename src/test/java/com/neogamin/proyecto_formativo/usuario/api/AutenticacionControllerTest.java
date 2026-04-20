package com.neogamin.proyecto_formativo.usuario.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neogamin.proyecto_formativo.compartido.api.GlobalExceptionHandler;
import com.neogamin.proyecto_formativo.usuario.api.dto.LoginRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.LoginResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.RegistroUsuarioRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.UsuarioResponse;
import com.neogamin.proyecto_formativo.usuario.aplicacion.AutenticacionServicio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AutenticacionControllerTest {

    @Mock
    private AutenticacionServicio autenticacionServicio;

    @InjectMocks
    private AutenticacionController autenticacionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnLoginResponse() throws Exception {
        MockMvc mockMvc = buildMockMvc();
        when(autenticacionServicio.login(any(LoginRequest.class), any()))
                .thenReturn(new LoginResponse("token", 1L, "Demo", "demo@neogaming.com", "ROLE_CLIENTE"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("demo@neogaming.com", "Clave123*"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.email").value("demo@neogaming.com"));
    }

    @Test
    void shouldReturnCreatedUserOnRegister() throws Exception {
        MockMvc mockMvc = buildMockMvc();
        when(autenticacionServicio.registrar(any(RegistroUsuarioRequest.class)))
                .thenReturn(new UsuarioResponse(5L, "Demo", "demo@neogaming.com", "3001234567", null, "CLIENTE", "ACTIVO"));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegistroUsuarioRequest("Demo", "demo@neogaming.com", "Clave123*", "3001234567"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("demo@neogaming.com"));
    }

    @Test
    void shouldReturnNoContentOnLogout() throws Exception {
        MockMvc mockMvc = buildMockMvc();
        doNothing().when(autenticacionServicio).logout(eq("Bearer token"));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }

    private MockMvc buildMockMvc() {
        return MockMvcBuilders.standaloneSetup(autenticacionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }
}
