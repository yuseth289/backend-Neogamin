package com.neogamin.proyecto_formativo.analitica.api;

import com.neogamin.proyecto_formativo.analitica.api.dto.PedidoEstadoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.ResumenVendedorResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.StockBajoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopProductoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaPeriodoResponse;
import com.neogamin.proyecto_formativo.analitica.aplicacion.ServicioAnaliticaVendedor;
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
@RequestMapping("/api/analitica/vendedor")
@RequiredArgsConstructor
@Tag(name = "Analitica - Vendedor", description = "Dashboard de ventas para el vendedor autenticado.")
public class ControladorAnaliticaVendedor {

    private final ServicioAnaliticaVendedor servicioAnaliticaVendedor;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenVendedorResponse> obtenerResumen() {
        return ResponseEntity.ok(servicioAnaliticaVendedor.obtenerResumen());
    }

    @GetMapping("/ventas-por-periodo")
    public ResponseEntity<List<VentaPeriodoResponse>> obtenerVentasPorPeriodo(
            @RequestParam PeriodoAnalitica periodo
    ) {
        return ResponseEntity.ok(servicioAnaliticaVendedor.obtenerVentasPorPeriodo(periodo));
    }

    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<TopProductoResponse>> obtenerProductosMasVendidos() {
        return ResponseEntity.ok(servicioAnaliticaVendedor.obtenerProductosMasVendidos());
    }

    @GetMapping("/pedidos-por-estado")
    public ResponseEntity<List<PedidoEstadoResponse>> obtenerPedidosPorEstado() {
        return ResponseEntity.ok(servicioAnaliticaVendedor.obtenerPedidosPorEstado());
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<StockBajoResponse>> obtenerStockBajo() {
        return ResponseEntity.ok(servicioAnaliticaVendedor.obtenerStockBajo());
    }
}
