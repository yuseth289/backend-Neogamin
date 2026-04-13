package com.neogamin.proyecto_formativo.analitica.aplicacion;

import com.neogamin.proyecto_formativo.analitica.api.dto.MetodoPagoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.PedidoEstadoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.ResumenAdminResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopProductoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopVendedorResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaCategoriaResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaPeriodoResponse;
import com.neogamin.proyecto_formativo.analitica.dominio.PeriodoAnalitica;
import com.neogamin.proyecto_formativo.analitica.infraestructura.AnaliticaRepositorio;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicioAnaliticaAdmin {

    private static final int LIMITE_TOP = 5;

    private final AnaliticaRepositorio analiticaRepositorio;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResumenAdminResponse obtenerResumen() {
        return analiticaRepositorio.obtenerResumenAdmin();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<VentaPeriodoResponse> obtenerVentasPorPeriodo(PeriodoAnalitica periodo) {
        return analiticaRepositorio.obtenerVentasPorPeriodoAdmin(periodo);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<TopVendedorResponse> obtenerTopVendedores() {
        return analiticaRepositorio.obtenerTopVendedores(LIMITE_TOP);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<TopProductoResponse> obtenerTopProductos() {
        return analiticaRepositorio.obtenerTopProductosAdmin(LIMITE_TOP);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<VentaCategoriaResponse> obtenerVentasPorCategoria() {
        return analiticaRepositorio.obtenerVentasPorCategoria();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<MetodoPagoResponse> obtenerMetodosPago() {
        return analiticaRepositorio.obtenerMetodosPagoMasUsados();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<PedidoEstadoResponse> obtenerPedidosPorEstado() {
        return analiticaRepositorio.obtenerPedidosPorEstadoAdmin();
    }
}
