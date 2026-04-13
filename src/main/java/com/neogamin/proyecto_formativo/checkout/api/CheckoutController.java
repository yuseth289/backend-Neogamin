package com.neogamin.proyecto_formativo.checkout.api;

import com.neogamin.proyecto_formativo.checkout.api.dto.ConfirmacionPedidoResponse;
import com.neogamin.proyecto_formativo.checkout.api.dto.GuardarEnvioRequest;
import com.neogamin.proyecto_formativo.checkout.api.dto.IniciarCheckoutResponse;
import com.neogamin.proyecto_formativo.checkout.api.dto.ProcesarPagoRequest;
import com.neogamin.proyecto_formativo.checkout.api.dto.ProcesarPagoResponse;
import com.neogamin.proyecto_formativo.checkout.aplicacion.CheckoutServicio;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Checkout", description = "Flujo de checkout y pasarela simulada.")
public class CheckoutController {

    private final CheckoutServicio checkoutServicio;

    @PostMapping
    public ResponseEntity<IniciarCheckoutResponse> iniciarCheckout() {
        return ResponseEntity.ok(checkoutServicio.iniciarCheckout());
    }

    @PostMapping("/envio")
    public ResponseEntity<IniciarCheckoutResponse> guardarEnvio(@Valid @RequestBody GuardarEnvioRequest request) {
        return ResponseEntity.ok(checkoutServicio.guardarEnvio(request));
    }

    @PostMapping("/pago")
    public ResponseEntity<ProcesarPagoResponse> procesarPago(@Valid @RequestBody ProcesarPagoRequest request) {
        return ResponseEntity.ok(checkoutServicio.procesarPago(request));
    }

    @GetMapping("/confirmacion/{numeroPedido}")
    public ResponseEntity<ConfirmacionPedidoResponse> obtenerConfirmacion(@PathVariable String numeroPedido) {
        return ResponseEntity.ok(checkoutServicio.obtenerConfirmacion(numeroPedido));
    }
}
