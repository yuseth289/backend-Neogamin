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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AnaliticaRepositorioImpl implements AnaliticaRepositorio {

    private static final String ESTADOS_VENTA = """
            ('pagado', 'preparando', 'enviado', 'entregado')
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ResumenVendedorResponse obtenerResumenVendedor(Long vendedorId) {
        var sql = """
                select
                    coalesce(sum(pd.total_linea), 0) as ingresos_totales,
                    coalesce(sum(case
                        when date_trunc('month', p.fecha_creacion) = date_trunc('month', now())
                        then pd.total_linea
                        else 0
                    end), 0) as ingresos_mes_actual,
                    count(distinct p.id_pedido) as cantidad_pedidos_vendidos,
                    coalesce(sum(pd.total_linea) / nullif(count(distinct p.id_pedido), 0), 0) as ticket_promedio
                from pedido_detalle pd
                join pedido p on p.id_pedido = pd.fk_pedido
                join producto pr on pr.id_producto = pd.fk_producto
                where pr.fk_vendedor = :vendedorId
                  and p.estado::text in """ + ESTADOS_VENTA;

        var fila = (Object[]) entityManager.createNativeQuery(sql)
                .setParameter("vendedorId", vendedorId)
                .getSingleResult();

        return new ResumenVendedorResponse(
                toBigDecimal(fila[0]),
                toBigDecimal(fila[1]),
                toLong(fila[2]),
                toBigDecimal(fila[3])
        );
    }

    @Override
    public List<VentaPeriodoResponse> obtenerVentasPorPeriodoVendedor(Long vendedorId, PeriodoAnalitica periodo) {
        var configuracion = configurarPeriodo(periodo);
        var sql = """
                select
                    date_trunc('%s', p.fecha_creacion) as periodo,
                    coalesce(sum(pd.total_linea), 0) as ingresos,
                    count(distinct p.id_pedido) as cantidad_pedidos
                from pedido_detalle pd
                join pedido p on p.id_pedido = pd.fk_pedido
                join producto pr on pr.id_producto = pd.fk_producto
                where pr.fk_vendedor = :vendedorId
                  and p.estado::text in %s
                  and p.fecha_creacion >= now() - interval '%s'
                group by 1
                order by 1 asc
                """.formatted(configuracion.truncado(), ESTADOS_VENTA, configuracion.intervalo());

        return mapearVentasPeriodo(
                entityManager.createNativeQuery(sql)
                        .setParameter("vendedorId", vendedorId)
                        .getResultList(),
                periodo
        );
    }

    @Override
    public List<TopProductoResponse> obtenerTopProductosVendedor(Long vendedorId, int limite) {
        var sql = """
                select
                    pr.id_producto,
                    pr.nombre,
                    pr.sku,
                    pr.slug,
                    coalesce(sum(pd.cantidad), 0) as unidades_vendidas,
                    coalesce(sum(pd.total_linea), 0) as ingresos_generados
                from pedido_detalle pd
                join pedido p on p.id_pedido = pd.fk_pedido
                join producto pr on pr.id_producto = pd.fk_producto
                where pr.fk_vendedor = :vendedorId
                  and p.estado::text in """ + ESTADOS_VENTA + """
                group by pr.id_producto, pr.nombre, pr.sku, pr.slug
                order by unidades_vendidas desc, ingresos_generados desc
                limit :limite
                """;

        return mapearTopProductos(
                entityManager.createNativeQuery(sql)
                        .setParameter("vendedorId", vendedorId)
                        .setParameter("limite", limite)
                        .getResultList()
        );
    }

    @Override
    public List<PedidoEstadoResponse> obtenerPedidosPorEstadoVendedor(Long vendedorId) {
        var sql = """
                select
                    p.estado::text as estado,
                    count(distinct p.id_pedido) as cantidad_pedidos
                from pedido p
                join pedido_detalle pd on pd.fk_pedido = p.id_pedido
                join producto pr on pr.id_producto = pd.fk_producto
                where pr.fk_vendedor = :vendedorId
                group by p.estado::text
                order by cantidad_pedidos desc, estado asc
                """;

        return mapearPedidosPorEstado(
                entityManager.createNativeQuery(sql)
                        .setParameter("vendedorId", vendedorId)
                        .getResultList()
        );
    }

    @Override
    public List<StockBajoResponse> obtenerStockBajoVendedor(Long vendedorId, int umbral) {
        var sql = """
                select
                    pr.id_producto,
                    pr.nombre,
                    pr.sku,
                    pr.stock_fisico,
                    pr.stock_reservado,
                    (pr.stock_fisico - pr.stock_reservado) as stock_disponible
                from producto pr
                where pr.fk_vendedor = :vendedorId
                  and pr.deleted_at is null
                  and pr.estado::text = 'activo'
                  and (pr.stock_fisico - pr.stock_reservado) <= :umbral
                order by stock_disponible asc, pr.nombre asc
                """;

        return mapearStockBajo(
                entityManager.createNativeQuery(sql)
                        .setParameter("vendedorId", vendedorId)
                        .setParameter("umbral", umbral)
                        .getResultList()
        );
    }

    @Override
    public ResumenAdminResponse obtenerResumenAdmin() {
        var sql = """
                select
                    coalesce(sum(p.total), 0) as ingresos_totales,
                    count(distinct p.id_pedido) as pedidos_totales,
                    coalesce(sum(p.total) / nullif(count(distinct p.id_pedido), 0), 0) as ticket_promedio_global,
                    count(distinct p.fk_usuario) as cantidad_clientes_activos
                from pedido p
                where p.estado::text in """ + ESTADOS_VENTA;

        var fila = (Object[]) entityManager.createNativeQuery(sql).getSingleResult();

        return new ResumenAdminResponse(
                toBigDecimal(fila[0]),
                toLong(fila[1]),
                toBigDecimal(fila[2]),
                toLong(fila[3])
        );
    }

    @Override
    public List<VentaPeriodoResponse> obtenerVentasPorPeriodoAdmin(PeriodoAnalitica periodo) {
        var configuracion = configurarPeriodo(periodo);
        var sql = """
                select
                    date_trunc('%s', p.fecha_creacion) as periodo,
                    coalesce(sum(p.total), 0) as ingresos,
                    count(distinct p.id_pedido) as cantidad_pedidos
                from pedido p
                where p.estado::text in %s
                  and p.fecha_creacion >= now() - interval '%s'
                group by 1
                order by 1 asc
                """.formatted(configuracion.truncado(), ESTADOS_VENTA, configuracion.intervalo());

        return mapearVentasPeriodo(entityManager.createNativeQuery(sql).getResultList(), periodo);
    }

    @Override
    public List<TopVendedorResponse> obtenerTopVendedores(int limite) {
        var sql = """
                select
                    u.id_usuario,
                    u.nombre,
                    u.email,
                    count(distinct p.id_pedido) as pedidos_vendidos,
                    coalesce(sum(pd.total_linea), 0) as ingresos_generados
                from pedido_detalle pd
                join pedido p on p.id_pedido = pd.fk_pedido
                join producto pr on pr.id_producto = pd.fk_producto
                join usuario u on u.id_usuario = pr.fk_vendedor
                where p.estado::text in """ + ESTADOS_VENTA + """
                group by u.id_usuario, u.nombre, u.email
                order by ingresos_generados desc, pedidos_vendidos desc
                limit :limite
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> filas = entityManager.createNativeQuery(sql)
                .setParameter("limite", limite)
                .getResultList();

        return filas.stream()
                .map(fila -> new TopVendedorResponse(
                        toLong(fila[0]),
                        toStringValue(fila[1]),
                        toStringValue(fila[2]),
                        toLong(fila[3]),
                        toBigDecimal(fila[4])
                ))
                .toList();
    }

    @Override
    public List<TopProductoResponse> obtenerTopProductosAdmin(int limite) {
        var sql = """
                select
                    pr.id_producto,
                    pr.nombre,
                    pr.sku,
                    pr.slug,
                    coalesce(sum(pd.cantidad), 0) as unidades_vendidas,
                    coalesce(sum(pd.total_linea), 0) as ingresos_generados
                from pedido_detalle pd
                join pedido p on p.id_pedido = pd.fk_pedido
                join producto pr on pr.id_producto = pd.fk_producto
                where p.estado::text in """ + ESTADOS_VENTA + """
                group by pr.id_producto, pr.nombre, pr.sku, pr.slug
                order by unidades_vendidas desc, ingresos_generados desc
                limit :limite
                """;

        return mapearTopProductos(
                entityManager.createNativeQuery(sql)
                        .setParameter("limite", limite)
                        .getResultList()
        );
    }

    @Override
    public List<VentaCategoriaResponse> obtenerVentasPorCategoria() {
        var sql = """
                select
                    c.id_categoria,
                    c.nombre,
                    coalesce(sum(pd.cantidad), 0) as unidades_vendidas,
                    coalesce(sum(pd.total_linea), 0) as ingresos_generados
                from pedido_detalle pd
                join pedido p on p.id_pedido = pd.fk_pedido
                join producto pr on pr.id_producto = pd.fk_producto
                join categoria c on c.id_categoria = pr.fk_categoria
                where p.estado::text in """ + ESTADOS_VENTA + """
                group by c.id_categoria, c.nombre
                order by ingresos_generados desc, unidades_vendidas desc
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> filas = entityManager.createNativeQuery(sql).getResultList();

        return filas.stream()
                .map(fila -> new VentaCategoriaResponse(
                        toLong(fila[0]),
                        toStringValue(fila[1]),
                        toLong(fila[2]),
                        toBigDecimal(fila[3])
                ))
                .toList();
    }

    @Override
    public List<MetodoPagoResponse> obtenerMetodosPagoMasUsados() {
        var sql = """
                select
                    pa.tipo_pago::text as metodo_pago,
                    count(pa.id_pago) as cantidad_usos,
                    coalesce(sum(pa.monto), 0) as monto_total
                from pago pa
                join pedido p on p.id_pedido = pa.fk_pedido
                where p.estado::text in """ + ESTADOS_VENTA + """
                  and pa.estado::text not in ('rechazado', 'anulado')
                group by pa.tipo_pago::text
                order by cantidad_usos desc, monto_total desc
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> filas = entityManager.createNativeQuery(sql).getResultList();

        return filas.stream()
                .map(fila -> new MetodoPagoResponse(
                        toStringValue(fila[0]),
                        toLong(fila[1]),
                        toBigDecimal(fila[2])
                ))
                .toList();
    }

    @Override
    public List<PedidoEstadoResponse> obtenerPedidosPorEstadoAdmin() {
        var sql = """
                select
                    p.estado::text as estado,
                    count(*) as cantidad_pedidos
                from pedido p
                group by p.estado::text
                order by cantidad_pedidos desc, estado asc
                """;

        return mapearPedidosPorEstado(entityManager.createNativeQuery(sql).getResultList());
    }

    private List<VentaPeriodoResponse> mapearVentasPeriodo(List<?> filas, PeriodoAnalitica periodo) {
        return filas.stream()
                .map(Object[].class::cast)
                .map(fila -> new VentaPeriodoResponse(
                        formatearPeriodo(fila[0], periodo),
                        toBigDecimal(fila[1]),
                        toLong(fila[2])
                ))
                .toList();
    }

    private List<TopProductoResponse> mapearTopProductos(List<?> filas) {
        return filas.stream()
                .map(Object[].class::cast)
                .map(fila -> new TopProductoResponse(
                        toLong(fila[0]),
                        toStringValue(fila[1]),
                        toStringValue(fila[2]),
                        toStringValue(fila[3]),
                        toLong(fila[4]),
                        toBigDecimal(fila[5])
                ))
                .toList();
    }

    private List<PedidoEstadoResponse> mapearPedidosPorEstado(List<?> filas) {
        return filas.stream()
                .map(Object[].class::cast)
                .map(fila -> new PedidoEstadoResponse(
                        toStringValue(fila[0]).toUpperCase(),
                        toLong(fila[1])
                ))
                .toList();
    }

    private List<StockBajoResponse> mapearStockBajo(List<?> filas) {
        return filas.stream()
                .map(Object[].class::cast)
                .map(fila -> new StockBajoResponse(
                        toLong(fila[0]),
                        toStringValue(fila[1]),
                        toStringValue(fila[2]),
                        toInteger(fila[3]),
                        toInteger(fila[4]),
                        toInteger(fila[5])
                ))
                .toList();
    }

    private String formatearPeriodo(Object valor, PeriodoAnalitica periodo) {
        var fecha = toOffsetDateTime(valor);
        return switch (periodo) {
            case DIARIO -> fecha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case SEMANAL -> "Semana " + fecha.format(DateTimeFormatter.ofPattern("yyyy-'W'ww"));
            case MENSUAL -> fecha.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        };
    }

    private OffsetDateTime toOffsetDateTime(Object valor) {
        if (valor instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }
        if (valor instanceof Timestamp timestamp) {
            return timestamp.toInstant().atOffset(ZoneOffset.UTC);
        }
        throw new IllegalArgumentException("No se pudo convertir el valor a fecha de periodo: " + valor);
    }

    private BigDecimal toBigDecimal(Object valor) {
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (valor instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(valor.toString());
    }

    private Long toLong(Object valor) {
        if (valor == null) {
            return 0L;
        }
        if (valor instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(valor.toString());
    }

    private Integer toInteger(Object valor) {
        if (valor == null) {
            return 0;
        }
        if (valor instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(valor.toString());
    }

    private String toStringValue(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private ConfiguracionPeriodo configurarPeriodo(PeriodoAnalitica periodo) {
        return switch (periodo) {
            case DIARIO -> new ConfiguracionPeriodo("day", "7 days");
            case SEMANAL -> new ConfiguracionPeriodo("week", "8 weeks");
            case MENSUAL -> new ConfiguracionPeriodo("month", "12 months");
        };
    }

    private record ConfiguracionPeriodo(String truncado, String intervalo) {
    }
}
