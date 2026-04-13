package com.neogamin.proyecto_formativo.catalogo.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusquedaNaturalProductoRequest {

    @NotBlank
    private String texto;

    private Boolean soloDisponibles = false;

    @Min(0)
    private Integer page = 0;

    @Min(1)
    private Integer size = 10;
}
