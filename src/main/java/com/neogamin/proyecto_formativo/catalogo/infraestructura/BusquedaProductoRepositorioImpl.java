package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class BusquedaProductoRepositorioImpl implements BusquedaProductoRepositorio {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Page<ResultadoBusquedaProductoFila> buscar(ConsultaBusquedaProducto consulta) {
        String documentoBusqueda = """
                (
                    setweight(to_tsvector('simple', coalesce(p.nombre, '')), 'A') ||
                    setweight(to_tsvector('simple', coalesce(c.nombre, '')), 'B') ||
                    setweight(to_tsvector('simple', coalesce(p.descripcion, '')), 'C')
                )
                """;
        String consultaFts = "to_tsquery('simple', :textoConsultaFts)";

        String precioActual = "coalesce(p.precio_vigente_cache, p.precio_lista)";
        String precioOrdenBusqueda = "least(coalesce(p.precio_vigente_cache, p.precio_lista), p.precio_lista)";
        String textoNormalizado = ":textoNormalizado";

        Map<String, Object> parametros = new LinkedHashMap<>();
        parametros.put("textoNormalizado", consulta.textoNormalizado());
        String filtroTipoProducto = construirFiltroTipoProducto(consulta, parametros);
        String ofertaVigente = """
                exists (
                    select 1
                    from oferta o
                    where o.fk_producto = p.id_producto
                      and o.estado = 'activo'::estado_generico
                      and now() >= o.fecha_inicio
                      and now() < o.fecha_fin
                )
                """;

        boolean tieneConsultaFts = consulta.textoConsultaFts() != null && !consulta.textoConsultaFts().isBlank();
        if (tieneConsultaFts) {
            parametros.put("textoConsultaFts", consulta.textoConsultaFts());
        }
        String puntaje = tieneConsultaFts
                ? """
                ts_rank(
                """
                + documentoBusqueda
                + """
                ,
                """
                + consultaFts
                + """
                )
                """
                : "0";
        String puntajeDifuso = """
                greatest(
                    similarity(lower(coalesce(p.nombre, '')), 
                """
                + textoNormalizado
                + """
                    ),
                    similarity(lower(coalesce(c.nombre, '')), 
                """
                + textoNormalizado
                + """
                    ),
                    similarity(lower(coalesce(p.descripcion, '')), 
                """
                + textoNormalizado
                + """
                    ),
                    similarity(lower(coalesce(p.sku, '')), 
                """
                + textoNormalizado
                + """
                    ),
                    word_similarity(
                """
                + textoNormalizado
                + """
                    , lower(coalesce(p.nombre, ''))
                    ),
                    word_similarity(
                """
                + textoNormalizado
                + """
                    , lower(coalesce(c.nombre, ''))
                    ),
                    word_similarity(
                """
                + textoNormalizado
                + """
                    , lower(coalesce(p.descripcion, ''))
                    ),
                    coalesce((
                        select max(word_similarity(token, lower(coalesce(p.nombre, ''))))
                        from regexp_split_to_table(
                            lower(
                                regexp_replace(
                                    cast(
                """
                + textoNormalizado
                + """
                                    as text),
                                    '\\s+',
                                    ' ',
                                    'g'
                                )
                            ),
                            ' '
                        ) as token
                        where length(token) >= 3
                    ), 0),
                    coalesce((
                        select max(word_similarity(token, lower(coalesce(c.nombre, ''))))
                        from regexp_split_to_table(
                            lower(
                                regexp_replace(
                                    cast(
                """
                + textoNormalizado
                + """
                                    as text),
                                    '\\s+',
                                    ' ',
                                    'g'
                                )
                            ),
                            ' '
                        ) as token
                        where length(token) >= 3
                    ), 0),
                    coalesce((
                        select max(word_similarity(token, lower(coalesce(p.sku, ''))))
                        from regexp_split_to_table(
                            lower(
                                regexp_replace(
                                    cast(
                """
                + textoNormalizado
                + """
                                    as text),
                                    '\\s+',
                                    ' ',
                                    'g'
                                )
                            ),
                            ' '
                        ) as token
                        where length(token) >= 3
                    ), 0)
                )
                """;
        String coincidenciaDifusa = """
                (
                    similarity(lower(coalesce(p.nombre, '')), 
                """
                + textoNormalizado
                + """
                    ) >= 0.16
                    or similarity(lower(coalesce(c.nombre, '')), 
                """
                + textoNormalizado
                + """
                    ) >= 0.14
                    or similarity(lower(coalesce(p.descripcion, '')), 
                """
                + textoNormalizado
                + """
                    ) >= 0.12
                    or similarity(lower(coalesce(p.sku, '')), 
                """
                + textoNormalizado
                + """
                    ) >= 0.24
                    or word_similarity(
                """
                + textoNormalizado
                + """
                    , lower(coalesce(p.nombre, ''))
                    ) >= 0.32
                    or word_similarity(
                """
                + textoNormalizado
                + """
                    , lower(coalesce(c.nombre, ''))
                    ) >= 0.28
                    or word_similarity(
                """
                + textoNormalizado
                + """
                    , lower(coalesce(p.descripcion, ''))
                    ) >= 0.24
                    or exists (
                        select 1
                        from regexp_split_to_table(
                            lower(
                                regexp_replace(
                                    cast(
                """
                + textoNormalizado
                + """
                                    as text),
                                    '\\s+',
                                    ' ',
                                    'g'
                                )
                            ),
                            ' '
                        ) as token
                        where length(token) >= 3
                          and (
                              word_similarity(token, lower(coalesce(p.nombre, ''))) >= 0.45
                              or word_similarity(token, lower(coalesce(c.nombre, ''))) >= 0.40
                              or word_similarity(token, lower(coalesce(p.sku, ''))) >= 0.55
                          )
                    )
                )
                """;

        String disponibilidad = consulta.soloDisponibles() ? " and (p.stock_fisico - p.stock_reservado) > 0 " : "";
        String filtroOferta = consulta.buscarSoloConOferta() ? " and " + ofertaVigente + " " : "";
        String condicionTexto = !tieneConsultaFts
                ? ""
                : """
                  and (
                """
                + documentoBusqueda
                + " @@ "
                + consultaFts
                + " or "
                + coincidenciaDifusa
                + ")";
        String ordenPrincipal = switch (consulta.intencionPrecio()) {
            case BAJO -> precioOrdenBusqueda + " asc, puntaje_relevancia desc, ";
            case ALTO -> precioOrdenBusqueda + " desc, puntaje_relevancia desc, ";
            case NEUTRO -> consulta.buscarSoloConOferta()
                    ? precioOrdenBusqueda + " asc, puntaje_relevancia desc, "
                    : "puntaje_relevancia desc, ";
        };

        String fromWhere = """
                from producto p
                join categoria c on c.id_categoria = p.fk_categoria
                where p.deleted_at is null
                  and p.estado = 'activo'::estado_generico
                  and c.deleted_at is null
                """
                + filtroTipoProducto
                + condicionTexto
                + filtroOferta
                + disponibilidad;

        String select = """
                select
                    p.id_producto,
                    p.nombre,
                    p.sku,
                    p.slug,
                    p.precio_lista,
                """
                + precioActual
                + """
                 as precio_vigente,
                    p.moneda,
                    (p.stock_fisico - p.stock_reservado) as stock_disponible,
                    c.nombre as nombre_categoria,
                    coalesce(
                        (
                            select pi.url_imagen
                            from producto_imagen pi
                            where pi.fk_producto = p.id_producto
                              and pi.deleted_at is null
                              and pi.es_principal = true
                            order by pi.orden asc
                            limit 1
                        ),
                        (
                            select pi.url_imagen
                            from producto_imagen pi
                            where pi.fk_producto = p.id_producto
                              and pi.deleted_at is null
                            order by pi.orden asc
                            limit 1
                        )
                    ) as url_imagen_principal,
                    (
                """
                + puntaje
                + """
                    +
                    (
                """
                + puntajeDifuso
                + """
                    ) * 10
                    +
                    case
                        when 
                """
                + ofertaVigente
                + """
                        then 8
                        else 0
                    end
                    +
                    case
                        when lower(coalesce(p.nombre, '')) like '%' || 
                """
                + textoNormalizado
                + """
                        || '%' then 5
                        else 0
                    end
                    +
                    case
                        when lower(coalesce(c.nombre, '')) like '%' || 
                """
                + textoNormalizado
                + """
                        || '%' then 2
                        else 0
                    end
                    ) as puntaje_relevancia
                """
                + fromWhere
                + """
                order by
                """
                + ordenPrincipal
                + """
                p.created_at desc, p.id_producto desc
                """;

        String count = "select count(*) " + fromWhere;

        Query query = entityManager.createNativeQuery(select);
        Query countQuery = entityManager.createNativeQuery(count);
        parametros.forEach((k, v) -> {
            if (select.contains(":" + k)) {
                query.setParameter(k, v);
            }
            if (count.contains(":" + k)) {
                countQuery.setParameter(k, v);
            }
        });

        query.setFirstResult(consulta.page() * consulta.size());
        query.setMaxResults(consulta.size());

        List<Object[]> filas = query.getResultList();
        long total = ((Number) countQuery.getSingleResult()).longValue();

        List<ResultadoBusquedaProductoFila> resultados = filas.stream()
                .map(this::mapearFila)
                .toList();

        return new PageImpl<>(resultados, PageRequest.of(consulta.page(), consulta.size()), total);
    }

    private ResultadoBusquedaProductoFila mapearFila(Object[] fila) {
        return new ResultadoBusquedaProductoFila(
                ((Number) fila[0]).longValue(),
                (String) fila[1],
                (String) fila[2],
                (String) fila[3],
                (BigDecimal) fila[4],
                (BigDecimal) fila[5],
                fila[6] == null ? null : fila[6].toString(),
                fila[7] == null ? 0 : ((Number) fila[7]).intValue(),
                (String) fila[8],
                (String) fila[9],
                fila[10] == null ? BigDecimal.ZERO : new BigDecimal(fila[10].toString())
        );
    }

    private String construirFiltroTipoProducto(ConsultaBusquedaProducto consulta, Map<String, Object> parametros) {
        if (consulta.aliasTipoProducto() == null || consulta.aliasTipoProducto().isEmpty()) {
            return "";
        }

        StringBuilder filtro = new StringBuilder(" and (");
        boolean primero = true;
        int indice = 0;

        for (String alias : consulta.aliasTipoProducto()) {
            if (alias == null || alias.isBlank()) {
                continue;
            }
            String parametro = "aliasTipoProducto" + indice++;
            parametros.put(parametro, "%" + alias.toLowerCase() + "%");

            if (!primero) {
                filtro.append(" or ");
            }
            filtro.append("lower(coalesce(c.nombre, '')) like :").append(parametro)
                    .append(" or lower(coalesce(p.nombre, '')) like :").append(parametro)
                    .append(" or lower(coalesce(p.descripcion, '')) like :").append(parametro);
            primero = false;
        }

        if (primero) {
            return "";
        }

        filtro.append(") ");
        return filtro.toString();
    }
}
