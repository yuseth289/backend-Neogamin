package com.neogamin.proyecto_formativo.catalogo.infraestructura;

import com.neogamin.proyecto_formativo.catalogo.api.dto.FiltroProductoRequest;
import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ProductoEspecificacion {

    private ProductoEspecificacion() {
    }

    public static Specification<ProductoEntidad> conFiltros(
            FiltroProductoRequest filtro,
            EstadoGenerico estado
    ) {
        return (root, query, cb) -> {
            query.distinct(true);
            return Specification.allOf(
                    noEliminados(),
                    porTexto(filtro.getTexto()),
                    porCategoria(filtro.getIdCategoria()),
                    porVendedor(filtro.getIdVendedor()),
                    porEstado(estado),
                    porMoneda(filtro.getMoneda()),
                    porPrecioMin(filtro.getPrecioMin()),
                    porPrecioMax(filtro.getPrecioMax()),
                    soloDisponibles(Boolean.TRUE.equals(filtro.getSoloDisponibles()))
            ).toPredicate(root, query, cb);
        };
    }

    public static Specification<ProductoEntidad> noEliminados() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ProductoEntidad> porTexto(String texto) {
        return (root, query, cb) -> {
            if (texto == null || texto.isBlank()) {
                return cb.conjunction();
            }
            var criterio = "%" + texto.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nombre")), criterio),
                    cb.like(cb.lower(root.get("sku")), criterio)
            );
        };
    }

    public static Specification<ProductoEntidad> porCategoria(Long idCategoria) {
        return (root, query, cb) -> idCategoria == null
                ? cb.conjunction()
                : cb.equal(root.get("categoria").get("id"), idCategoria);
    }

    public static Specification<ProductoEntidad> porVendedor(Long idVendedor) {
        return (root, query, cb) -> idVendedor == null
                ? cb.conjunction()
                : cb.equal(root.get("vendedor").get("id"), idVendedor);
    }

    public static Specification<ProductoEntidad> porEstado(EstadoGenerico estado) {
        return (root, query, cb) -> estado == null
                ? cb.conjunction()
                : cb.equal(root.get("estado"), estado);
    }

    public static Specification<ProductoEntidad> porMoneda(String moneda) {
        return (root, query, cb) -> {
            if (moneda == null || moneda.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.upper(root.get("moneda")), moneda.trim().toUpperCase());
        };
    }

    public static Specification<ProductoEntidad> porPrecioMin(BigDecimal precioMin) {
        return (root, query, cb) -> precioMin == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("precioLista"), precioMin);
    }

    public static Specification<ProductoEntidad> porPrecioMax(BigDecimal precioMax) {
        return (root, query, cb) -> precioMax == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("precioLista"), precioMax);
    }

    public static Specification<ProductoEntidad> soloDisponibles(boolean soloDisponibles) {
        return (root, query, cb) -> {
            if (!soloDisponibles) {
                return cb.conjunction();
            }
            return cb.greaterThan(
                    cb.diff(root.get("stockFisico"), root.get("stockReservado")),
                    0
            );
        };
    }
}
