package com.neogamin.proyecto_formativo.interaccion.api;

import com.neogamin.proyecto_formativo.interaccion.api.dto.EstadoInteraccionResponse;
import com.neogamin.proyecto_formativo.interaccion.aplicacion.InteraccionServicio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interacciones")
@RequiredArgsConstructor
@Tag(name = "Interaccion", description = "Like y wishlist sobre productos.")
public class InteraccionController {

    private final InteraccionServicio interaccionServicio;

    @PostMapping("/productos/{productoId}/like")
    public ResponseEntity<EstadoInteraccionResponse> toggleLike(@PathVariable Long productoId) {
        return ResponseEntity.ok(interaccionServicio.toggleLike(productoId));
    }

    @PostMapping("/productos/{productoId}/wishlist")
    public ResponseEntity<EstadoInteraccionResponse> toggleWishlist(@PathVariable Long productoId) {
        return ResponseEntity.ok(interaccionServicio.toggleDeseado(productoId));
    }
}
