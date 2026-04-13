package com.neogamin.proyecto_formativo.catalogo.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FiltroProductoRequest {

    private String texto;
    private Long idCategoria;
    private Long idVendedor;
    private String estado;
    private String moneda;

    @DecimalMin("0.0")
    private BigDecimal precioMin;

    @DecimalMin("0.0")
    private BigDecimal precioMax;

    private Boolean soloDisponibles;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    private Integer size = 10;

    private String[] sort;
}
