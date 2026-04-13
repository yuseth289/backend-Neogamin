package com.neogamin.proyecto_formativo.analitica.aplicacion;

import com.neogamin.proyecto_formativo.analitica.api.dto.PedidoEstadoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.ResumenVendedorResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.StockBajoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopProductoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaPeriodoResponse;
import com.neogamin.proyecto_formativo.analitica.dominio.PeriodoAnalitica;
import com.neogamin.proyecto_formativo.analitica.infraestructura.AnaliticaRepositorio;
import com.neogamin.proyecto_formativo.compartido.seguridad.SeguridadUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioAnaliticaVendedor {

    private static final int LIMITE_TOP_PRODUCTOS = 5;
    private static final int UMBRAL_STOCK_BAJO = 10;

    private final AnaliticaRepositorio analiticaRepositorio;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResumenVendedorResponse obtenerResumen() {
        return analiticaRepositorio.obtenerResumenVendedor(SeguridadUtils.usuarioId());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('VENDEDOR')")
    public List<VentaPeriodoResponse> obtenerVentasPorPeriodo(PeriodoAnalitica periodo) {
        return analiticaRepositorio.obtenerVentasPorPeriodoVendedor(SeguridadUtils.usuarioId(), periodo);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('VENDEDOR')")
    public List<TopProductoResponse> obtenerProductosMasVendidos() {
        return analiticaRepositorio.obtenerTopProductosVendedor(SeguridadUtils.usuarioId(), LIMITE_TOP_PRODUCTOS);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('VENDEDOR')")
    public List<PedidoEstadoResponse> obtenerPedidosPorEstado() {
        return analiticaRepositorio.obtenerPedidosPorEstadoVendedor(SeguridadUtils.usuarioId());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('VENDEDOR')")
    public List<StockBajoResponse> obtenerStockBajo() {
        return analiticaRepositorio.obtenerStockBajoVendedor(SeguridadUtils.usuarioId(), UMBRAL_STOCK_BAJO);
    }
}
