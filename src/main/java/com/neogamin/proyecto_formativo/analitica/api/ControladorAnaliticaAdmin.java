package com.neogamin.proyecto_formativo.analitica.api;

import com.neogamin.proyecto_formativo.analitica.api.dto.MetodoPagoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.PedidoEstadoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.ResumenAdminResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopProductoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopVendedorResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaCategoriaResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaPeriodoResponse;
import com.neogamin.proyecto_formativo.analitica.aplicacion.ServicioAnaliticaAdmin;
import com.neogamin.proyecto_formativo.analitica.dominio.PeriodoAnalitica;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analitica/admin")
@RequiredArgsConstructor
@Tag(name = "Analitica - Admin", description = "Dashboard global de ventas para administradores.")
public class ControladorAnaliticaAdmin {

    private final ServicioAnaliticaAdmin servicioAnaliticaAdmin;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenAdminResponse> obtenerResumen() {
        return ResponseEntity.ok(servicioAnaliticaAdmin.obtenerResumen());
    }

    @GetMapping("/ventas-por-periodo")
    public ResponseEntity<List<VentaPeriodoResponse>> obtenerVentasPorPeriodo(
            @RequestParam PeriodoAnalitica periodo
    ) {
        return ResponseEntity.ok(servicioAnaliticaAdmin.obtenerVentasPorPeriodo(periodo));
    }

    @GetMapping("/top-vendedores")
    public ResponseEntity<List<TopVendedorResponse>> obtenerTopVendedores() {
        return ResponseEntity.ok(servicioAnaliticaAdmin.obtenerTopVendedores());
    }

    @GetMapping("/top-productos")
    public ResponseEntity<List<TopProductoResponse>> obtenerTopProductos() {
        return ResponseEntity.ok(servicioAnaliticaAdmin.obtenerTopProductos());
    }

    @GetMapping("/ventas-por-categoria")
    public ResponseEntity<List<VentaCategoriaResponse>> obtenerVentasPorCategoria() {
        return ResponseEntity.ok(servicioAnaliticaAdmin.obtenerVentasPorCategoria());
    }

    @GetMapping("/metodos-pago")
    public ResponseEntity<List<MetodoPagoResponse>> obtenerMetodosPago() {
        return ResponseEntity.ok(servicioAnaliticaAdmin.obtenerMetodosPago());
    }

    @GetMapping("/pedidos-por-estado")
    public ResponseEntity<List<PedidoEstadoResponse>> obtenerPedidosPorEstado() {
        return ResponseEntity.ok(servicioAnaliticaAdmin.obtenerPedidosPorEstado());
    }
}
