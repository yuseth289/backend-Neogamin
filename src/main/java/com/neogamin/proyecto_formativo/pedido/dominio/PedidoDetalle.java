package com.neogamin.proyecto_formativo.pedido.dominio;

import com.neogamin.proyecto_formativo.catalogo.dominio.Producto;
import com.neogamin.proyecto_formativo.compartido.dominio.EntidadBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pedido_detalle")
public class PedidoDetalle extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_producto")
    private Producto producto;

    @Column(name = "producto_sku", nullable = false, length = 80)
    private String productoSku;

    @Column(name = "producto_nombre", nullable = false, length = 180)
    private String productoNombre;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String moneda;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_lista_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioListaUnitario;

    @Column(name = "descuento_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal descuentoUnitario = BigDecimal.ZERO;

    @Column(name = "precio_final_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal precioFinalUnitario;

    @Column(name = "impuesto_unitario", nullable = false, precision = 14, scale = 2)
    private BigDecimal impuestoUnitario = BigDecimal.ZERO;

    @Column(name = "subtotal_linea", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotalLinea;

    @Column(name = "total_linea", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalLinea;
}
