package com.neogamin.proyecto_formativo.usuario.dominio;

import com.neogamin.proyecto_formativo.compartido.dominio.EntidadBase;
import com.neogamin.proyecto_formativo.compartido.dominio.EstadoGenerico;
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
@Table(name = "direccion")
public class Direccion extends EntidadBase {

    public enum TipoDireccion {
        ENVIO,
        FACTURACION
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_usuario")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDireccion tipo;

    @Column(name = "es_principal", nullable = false)
    private Boolean principal = false;

    @Column(nullable = false, length = 80)
    private String pais;

    @Column(length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(length = 100)
    private String comuna;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(nullable = false, length = 150)
    private String calle;

    @Column(nullable = false, length = 30)
    private String numero;

    @Column(length = 255)
    private String referencia;

    @Column(nullable = false)
    private EstadoGenerico estado;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
