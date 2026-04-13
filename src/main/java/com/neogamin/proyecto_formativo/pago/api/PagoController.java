package com.neogamin.proyecto_formativo.pago.api;

import com.neogamin.proyecto_formativo.pago.api.dto.PagoResponse;
import com.neogamin.proyecto_formativo.pago.aplicacion.PagoServicio;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pago", description = "Procesamiento y consulta de pagos.")
public class PagoController {

    private final PagoServicio pagoServicio;

    @PostMapping("/{pagoId}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ResponseEntity<PagoResponse> aprobar(@PathVariable Long pagoId) {
        return ResponseEntity.ok(pagoServicio.aprobarPago(pagoId));
    }

    @PostMapping("/{pagoId}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
    public ResponseEntity<PagoResponse> rechazar(@PathVariable Long pagoId) {
        return ResponseEntity.ok(pagoServicio.rechazarPago(pagoId));
    }

    @GetMapping("/{pagoId}")
    public ResponseEntity<PagoResponse> obtener(@PathVariable Long pagoId) {
        return ResponseEntity.ok(pagoServicio.obtener(pagoId));
    }
}
