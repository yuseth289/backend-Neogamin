package com.neogamin.proyecto_formativo.inventario.dominio;

import com.neogamin.proyecto_formativo.catalogo.dominio.ProductoEntidad;
import com.neogamin.proyecto_formativo.pedido.dominio.Pedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "producto_stock_movimiento")
public class ProductoStockMovimientoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_producto")
    private ProductoEntidad producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_pedido")
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimientoStock tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_fisico_anterior", nullable = false)
    private Integer stockFisicoAnterior;

    @Column(name = "stock_fisico_nuevo", nullable = false)
    private Integer stockFisicoNuevo;

    @Column(name = "stock_reservado_anterior", nullable = false)
    private Integer stockReservadoAnterior;

    @Column(name = "stock_reservado_nuevo", nullable = false)
    private Integer stockReservadoNuevo;

    @Column(length = 255)
    private String motivo;

    @Column(name = "fecha_movimiento", nullable = false)
    private OffsetDateTime fechaMovimiento;
}
