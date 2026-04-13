package com.neogamin.proyecto_formativo.analitica.infraestructura;

import com.neogamin.proyecto_formativo.analitica.api.dto.MetodoPagoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.PedidoEstadoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.ResumenAdminResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.ResumenVendedorResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.StockBajoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopProductoResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.TopVendedorResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaCategoriaResponse;
import com.neogamin.proyecto_formativo.analitica.api.dto.VentaPeriodoResponse;
import com.neogamin.proyecto_formativo.analitica.dominio.PeriodoAnalitica;
import java.util.List;

public interface AnaliticaRepositorio {

    ResumenVendedorResponse obtenerResumenVendedor(Long vendedorId);

    List<VentaPeriodoResponse> obtenerVentasPorPeriodoVendedor(Long vendedorId, PeriodoAnalitica periodo);

    List<TopProductoResponse> obtenerTopProductosVendedor(Long vendedorId, int limite);

    List<PedidoEstadoResponse> obtenerPedidosPorEstadoVendedor(Long vendedorId);

    List<StockBajoResponse> obtenerStockBajoVendedor(Long vendedorId, int umbral);

    ResumenAdminResponse obtenerResumenAdmin();

    List<VentaPeriodoResponse> obtenerVentasPorPeriodoAdmin(PeriodoAnalitica periodo);

    List<TopVendedorResponse> obtenerTopVendedores(int limite);

    List<TopProductoResponse> obtenerTopProductosAdmin(int limite);

    List<VentaCategoriaResponse> obtenerVentasPorCategoria();

    List<MetodoPagoResponse> obtenerMetodosPagoMasUsados();

    List<PedidoEstadoResponse> obtenerPedidosPorEstadoAdmin();
}
