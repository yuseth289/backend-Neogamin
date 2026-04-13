package com.neogamin.proyecto_formativo.catalogo.dominio;

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
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "producto_imagen")
public class ProductoImagenEntidad extends EntidadBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_producto")
    private ProductoEntidad producto;

    @Column(name = "url_imagen", nullable = false)
    private String urlImagen;

    @Column(name = "alt_text", length = 180)
    private String altText;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "es_principal", nullable = false)
    private Boolean principal;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
