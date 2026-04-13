package com.neogamin.proyecto_formativo.resena.api;

import com.neogamin.proyecto_formativo.compartido.infraestructura.OpenApiConfiguracion;
import com.neogamin.proyecto_formativo.resena.api.dto.CrearOActualizarResenaRequest;
import com.neogamin.proyecto_formativo.resena.api.dto.ResenaProductoResponse;
import com.neogamin.proyecto_formativo.resena.api.dto.ResumenCalificacionProductoResponse;
import com.neogamin.proyecto_formativo.resena.aplicacion.ResenaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resenas")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Gestión de reseñas y calificaciones de productos.")
public class ResenaController {

    private final ResenaServicio resenaServicio;

    @PostMapping
    @Operation(summary = "Crear o actualizar la reseña del usuario autenticado")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<ResenaProductoResponse> crearOActualizar(
            @Valid @RequestBody CrearOActualizarResenaRequest request
    ) {
        return ResponseEntity.ok(resenaServicio.crearOActualizar(request));
    }

    @GetMapping("/productos/{productoId}")
    @Operation(summary = "Listar reseñas visibles de un producto")
    public ResponseEntity<List<ResenaProductoResponse>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(resenaServicio.listarPorProducto(productoId));
    }

    @GetMapping("/productos/{productoId}/resumen")
    @Operation(summary = "Obtener el resumen de calificación de un producto")
    public ResponseEntity<ResumenCalificacionProductoResponse> obtenerResumen(@PathVariable Long productoId) {
        return ResponseEntity.ok(resenaServicio.obtenerResumenProducto(productoId));
    }

    @DeleteMapping("/{resenaId}")
    @Operation(summary = "Eliminar lógicamente una reseña")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<Void> eliminar(@PathVariable Long resenaId) {
        resenaServicio.eliminar(resenaId);
        return ResponseEntity.noContent().build();
    }
}
