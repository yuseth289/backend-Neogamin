package com.neogamin.proyecto_formativo.catalogo.api;

import com.neogamin.proyecto_formativo.catalogo.api.dto.BusquedaNaturalProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.ProductoBusquedaResponse;
import com.neogamin.proyecto_formativo.catalogo.aplicacion.ServicioBusquedaProducto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo/productos")
@RequiredArgsConstructor
@Tag(name = "Catalogo - Busqueda", description = "Busqueda natural y semantica de productos.")
public class ControladorBusquedaProducto {

    private final ServicioBusquedaProducto servicioBusquedaProducto;

    @GetMapping("/buscar-natural")
    public ResponseEntity<Page<ProductoBusquedaResponse>> buscar(@Valid @ModelAttribute BusquedaNaturalProductoRequest request) {
        return ResponseEntity.ok(servicioBusquedaProducto.buscar(request));
    }
}
