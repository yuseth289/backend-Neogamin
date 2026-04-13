package com.neogamin.proyecto_formativo.facturacion.api;

import com.neogamin.proyecto_formativo.facturacion.api.dto.FacturaResponse;
import com.neogamin.proyecto_formativo.facturacion.aplicacion.FacturacionServicio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@Tag(name = "Facturacion", description = "Consulta de facturas emitidas.")
public class FacturaController {

    private final FacturacionServicio facturacionServicio;

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<FacturaResponse> obtenerPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(facturacionServicio.obtenerPorPedido(pedidoId));
    }
}
