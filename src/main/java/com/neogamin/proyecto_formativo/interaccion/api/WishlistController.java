package com.neogamin.proyecto_formativo.interaccion.api;

import com.neogamin.proyecto_formativo.interaccion.api.dto.WishlistProductoResponse;
import com.neogamin.proyecto_formativo.interaccion.aplicacion.InteraccionServicio;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interaccion")
@RequiredArgsConstructor
@Tag(name = "Interaccion - Wishlist", description = "Consulta de wishlist del usuario autenticado.")
public class WishlistController {

    private final InteraccionServicio interaccionServicio;

    @GetMapping("/wishlist")
    public ResponseEntity<List<WishlistProductoResponse>> listarWishlist() {
        return ResponseEntity.ok(interaccionServicio.listarWishlist());
    }
}
