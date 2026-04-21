package com.neogamin.proyecto_formativo.catalogo.dominio;

import com.neogamin.proyecto_formativo.compartido.dominio.EntidadBase;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
import com.neogamin.proyecto_formativo.usuario.dominio.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "producto")
public class ProductoEntidad extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_vendedor")
    private Usuario vendedor;

    @Column(nullable = false, length = 80)
    private String sku;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String moneda;

    @Column(name = "precio_lista", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioLista;

    @Column(name = "precio_vigente_cache", precision = 14, scale = 2)
    private BigDecimal precioVigenteCache;

    @Column(name = "stock_fisico", nullable = false)
    private Integer stockFisico;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado;

    @Column(name = "needs_recalc", nullable = false)
    private Boolean needsRecalc;

    @Column(length = 50)
    private String condicion;

    @Column(nullable = false)
    private EstadoGenerico estado;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "producto")
    private List<ProductoImagenEntidad> imagenes = new ArrayList<>();
}
