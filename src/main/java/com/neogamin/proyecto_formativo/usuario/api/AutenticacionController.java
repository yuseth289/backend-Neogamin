package com.neogamin.proyecto_formativo.usuario.api;

import com.neogamin.proyecto_formativo.usuario.api.dto.LoginRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.LoginResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.RegistroUsuarioRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.UsuarioResponse;
import com.neogamin.proyecto_formativo.usuario.aplicacion.AutenticacionServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Usuario - Autenticacion", description = "Endpoints de autenticacion y emision de JWT.")
public class AutenticacionController {

    private final AutenticacionServicio autenticacionServicio;

    @PostMapping("/registro")
    @Operation(
            summary = "Registrarse",
            description = "Registra un nuevo usuario cliente activo en el sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Correo duplicado o payload invalido")
    })
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autenticacionServicio.registrar(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesion",
            description = "Autentica un usuario y devuelve un JWT listo para usar en el boton Authorize de Swagger."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesion iniciada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "Login exitoso",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiJ9...",
                                              "usuarioId": 2,
                                              "nombre": "Yuseth Perez",
                                              "email": "yuseth@correo.com",
                                              "rol": "ADMIN"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Credenciales invalidas o payload mal formado"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            @Parameter(hidden = true) HttpServletRequest servletRequest
    ) {
        return ResponseEntity.ok(autenticacionServicio.login(request, servletRequest));
    }
}
