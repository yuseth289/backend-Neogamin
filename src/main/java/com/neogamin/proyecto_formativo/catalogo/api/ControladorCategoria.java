package com.neogamin.proyecto_formativo.catalogo.api;

import com.neogamin.proyecto_formativo.catalogo.api.dto.CategoriaArbolResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CategoriaResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CrearCategoriaRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.FiltroProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoListadoResponse;
import com.neogamin.proyecto_formativo.catalogo.aplicacion.ServicioCategoria;
import com.neogamin.proyecto_formativo.compartido.infraestructura.OpenApiConfiguracion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo/categorias")
@RequiredArgsConstructor
@Tag(name = "Catalogo - Categorias", description = "Consulta de categorias y navegacion por arbol.")
public class ControladorCategoria {

    private final ServicioCategoria servicioCategoria;

    @PostMapping
    @Operation(summary = "Crear categoria")
    @SecurityRequirement(name = OpenApiConfiguracion.ESQUEMA_SEGURIDAD_BEARER)
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CrearCategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioCategoria.crearCategoria(request));
    }

    @GetMapping("/arbol")
    public ResponseEntity<List<CategoriaArbolResponse>> listarArbol() {
        return ResponseEntity.ok(servicioCategoria.listarArbol());
    }

    @GetMapping("/{idCategoria}/productos")
    public ResponseEntity<Page<ProductoListadoResponse>> listarProductosPorCategoria(
            @PathVariable Long idCategoria,
            @Valid @ModelAttribute FiltroProductoRequest filtro
    ) {
        return ResponseEntity.ok(servicioCategoria.listarProductosPorCategoria(idCategoria, filtro));
    }
}
