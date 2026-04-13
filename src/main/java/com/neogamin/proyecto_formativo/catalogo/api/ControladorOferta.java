package com.neogamin.proyecto_formativo.catalogo.api;

import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaActivaResponse;
import com.neogamin.proyecto_formativo.catalogo.api.dto.CrearOfertaRequest;
import com.neogamin.proyecto_formativo.catalogo.api.dto.OfertaResponse;
import com.neogamin.proyecto_formativo.catalogo.aplicacion.ServicioOferta;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo/ofertas")
@RequiredArgsConstructor
@Tag(name = "Catalogo - Ofertas", description = "Gestion y consulta de ofertas comerciales.")
public class ControladorOferta {

    private final ServicioOferta servicioOferta;

    @GetMapping("/activas")
    public ResponseEntity<List<OfertaActivaResponse>> listarActivas() {
        return ResponseEntity.ok(servicioOferta.listarOfertasActivas());
    }

    @PostMapping
    public ResponseEntity<OfertaResponse> crear(@Valid @RequestBody CrearOfertaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioOferta.crearOferta(request));
    }
}
