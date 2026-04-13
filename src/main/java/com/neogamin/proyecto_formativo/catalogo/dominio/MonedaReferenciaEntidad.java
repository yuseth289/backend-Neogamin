package com.neogamin.proyecto_formativo.catalogo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "moneda_referencia")
public class MonedaReferenciaEntidad {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "codigo", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 10)
    private String simbolo;

    @Column(nullable = false)
    private Boolean activa;
}
