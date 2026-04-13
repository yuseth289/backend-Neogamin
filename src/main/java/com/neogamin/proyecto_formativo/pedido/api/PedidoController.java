package com.neogamin.proyecto_formativo.pedido.api;

import com.neogamin.proyecto_formativo.pedido.api.dto.AgregarItemPedidoRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.CheckoutRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.CheckoutResponse;
import com.neogamin.proyecto_formativo.pedido.api.dto.CrearPedidoRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.FiltroMisPedidosRequest;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoListadoResponse;
import com.neogamin.proyecto_formativo.pedido.api.dto.PedidoResponse;
import com.neogamin.proyecto_formativo.pedido.aplicacion.PedidoServicio;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedido", description = "Creacion, consulta y checkout de pedidos.")
public class PedidoController {

    private final PedidoServicio pedidoServicio;

    @GetMapping("/mis-pedidos")
    public ResponseEntity<Page<PedidoListadoResponse>> listarMisPedidos(
            @Valid @ModelAttribute FiltroMisPedidosRequest filtro
    ) {
        return ResponseEntity.ok(pedidoServicio.listarMisPedidos(filtro));
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@RequestBody(required = false) CrearPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoServicio.crearPedido(request == null ? new CrearPedidoRequest("COP", null, null) : request));
    }

    @PostMapping("/{pedidoId}/items")
    public ResponseEntity<PedidoResponse> agregarItem(@PathVariable Long pedidoId, @Valid @RequestBody AgregarItemPedidoRequest request) {
        return ResponseEntity.ok(pedidoServicio.agregarItem(pedidoId, request));
    }

    @PostMapping("/{pedidoId}/recalcular")
    public ResponseEntity<PedidoResponse> recalcular(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoServicio.recalcularPedido(pedidoId));
    }

    @PostMapping("/{pedidoId}/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable Long pedidoId, @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(pedidoServicio.checkout(pedidoId, request));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pedidoServicio.obtener(pedidoId));
    }
}
