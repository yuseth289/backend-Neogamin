package com.neogamin.proyecto_formativo.carrito.api;

import com.neogamin.proyecto_formativo.carrito.api.dto.ActualizarCantidadCarritoRequest;
import com.neogamin.proyecto_formativo.carrito.api.dto.AgregarProductoCarritoRequest;
import com.neogamin.proyecto_formativo.carrito.api.dto.CarritoResponse;
import com.neogamin.proyecto_formativo.carrito.aplicacion.CarritoServicio;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Carrito", description = "Gestion completa del carrito de compra del usuario autenticado.")
public class CarritoController {

    private final CarritoServicio carritoServicio;

    @GetMapping
    public ResponseEntity<CarritoResponse> obtenerMiCarrito() {
        return ResponseEntity.ok(carritoServicio.obtenerMiCarrito());
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponse> agregarProducto(@Valid @RequestBody AgregarProductoCarritoRequest request) {
        return ResponseEntity.ok(carritoServicio.agregarProducto(request));
    }

    @PatchMapping("/items/{idItem}")
    public ResponseEntity<CarritoResponse> actualizarCantidad(
            @PathVariable Long idItem,
            @Valid @RequestBody ActualizarCantidadCarritoRequest request
    ) {
        return ResponseEntity.ok(carritoServicio.actualizarCantidad(idItem, request));
    }

    @DeleteMapping("/items/{idItem}")
    public ResponseEntity<CarritoResponse> eliminarItem(@PathVariable Long idItem) {
        return ResponseEntity.ok(carritoServicio.eliminarItem(idItem));
    }

    @DeleteMapping
    public ResponseEntity<CarritoResponse> vaciarCarrito() {
        return ResponseEntity.ok(carritoServicio.vaciarCarrito());
    }

    @PostMapping("/convertir-a-pedido")
    public ResponseEntity<PedidoResponse> convertirAPedido() {
        return ResponseEntity.ok(carritoServicio.convertirAPedido());
    }
}
