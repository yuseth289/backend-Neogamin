package com.neogamin.proyecto_formativo.catalogo.dominio;

import com.neogamin.proyecto_formativo.compartido.dominio.EntidadBase;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "oferta")
public class OfertaEntidad extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_producto")
    private ProductoEntidad producto;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "porcentaje_desc", precision = 5, scale = 2)
    private BigDecimal porcentajeDesc;

    @Column(name = "precio_oferta", precision = 14, scale = 2)
    private BigDecimal precioOferta;

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private OffsetDateTime fechaFin;

    @Column(nullable = false)
    private EstadoGenerico estado;
}
