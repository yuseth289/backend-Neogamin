package com.neogamin.proyecto_formativo.usuario.api;

import com.neogamin.proyecto_formativo.usuario.api.dto.UsuarioResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.ActualizarPerfilUsuarioRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.ConvertirVendedorRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.DireccionResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.GuardarDireccionRequest;
import com.neogamin.proyecto_formativo.usuario.api.dto.PerfilUsuarioResponse;
import com.neogamin.proyecto_formativo.usuario.api.dto.VendedorResponse;
import com.neogamin.proyecto_formativo.usuario.aplicacion.ServicioVendedor;
import com.neogamin.proyecto_formativo.usuario.aplicacion.UsuarioServicio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuario", description = "Consulta de perfil y operaciones del usuario autenticado.")
public class UsuarioController {

    private final UsuarioServicio usuarioServicio;
    private final ServicioVendedor servicioVendedor;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me() {
        return ResponseEntity.ok(usuarioServicio.perfilActual());
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilUsuarioResponse> obtenerPerfil() {
        return ResponseEntity.ok(usuarioServicio.obtenerPerfilActual());
    }

    @PutMapping("/perfil")
    public ResponseEntity<PerfilUsuarioResponse> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilUsuarioRequest request
    ) {
        return ResponseEntity.ok(usuarioServicio.actualizarPerfilActual(request));
    }

    @GetMapping("/direcciones")
    public ResponseEntity<List<DireccionResponse>> listarDirecciones() {
        return ResponseEntity.ok(usuarioServicio.listarMisDirecciones());
    }

    @GetMapping("/direcciones/{idDireccion}")
    public ResponseEntity<DireccionResponse> obtenerDireccion(@PathVariable Long idDireccion) {
        return ResponseEntity.ok(usuarioServicio.obtenerMiDireccion(idDireccion));
    }

    @PostMapping("/direcciones")
    public ResponseEntity<DireccionResponse> crearDireccion(@Valid @RequestBody GuardarDireccionRequest request) {
        return ResponseEntity.ok(usuarioServicio.crearDireccion(request));
    }

    @PutMapping("/direcciones/{idDireccion}")
    public ResponseEntity<DireccionResponse> actualizarDireccion(
            @PathVariable Long idDireccion,
            @Valid @RequestBody GuardarDireccionRequest request
    ) {
        return ResponseEntity.ok(usuarioServicio.actualizarDireccion(idDireccion, request));
    }

    @PatchMapping("/direcciones/{idDireccion}/principal")
    public ResponseEntity<DireccionResponse> marcarPrincipal(@PathVariable Long idDireccion) {
        return ResponseEntity.ok(usuarioServicio.marcarPrincipal(idDireccion));
    }

    @DeleteMapping("/direcciones/{idDireccion}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Long idDireccion) {
        usuarioServicio.eliminarDireccion(idDireccion);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/convertir-vendedor")
    public ResponseEntity<VendedorResponse> convertirEnVendedor(
            @Valid @RequestBody ConvertirVendedorRequest request
    ) {
        return ResponseEntity.ok(servicioVendedor.convertirEnVendedor(request));
    }
}
