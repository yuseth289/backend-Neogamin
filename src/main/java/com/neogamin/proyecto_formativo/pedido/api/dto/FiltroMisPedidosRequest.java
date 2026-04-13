package com.neogamin.proyecto_formativo.pedido.api.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FiltroMisPedidosRequest {

    private String estado;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    private Integer size = 10;
}
