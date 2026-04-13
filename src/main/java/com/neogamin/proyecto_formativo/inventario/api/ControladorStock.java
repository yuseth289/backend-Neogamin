package com.neogamin.proyecto_formativo.inventario.api;

import com.neogamin.proyecto_formativo.inventario.api.dto.AjustarStockProductoRequest;
import com.neogamin.proyecto_formativo.inventario.api.dto.StockProductoResponse;
import com.neogamin.proyecto_formativo.inventario.aplicacion.ServicioStock;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventario/productos")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Ajustes y operaciones de inventario.")
public class ControladorStock {

    private final ServicioStock servicioStock;

    @PatchMapping("/{idProducto}/stock")
    public ResponseEntity<StockProductoResponse> ajustarStock(
            @PathVariable Long idProducto,
            @Valid @RequestBody AjustarStockProductoRequest request
    ) {
        return ResponseEntity.ok(servicioStock.ajustarStock(idProducto, request));
    }
}
