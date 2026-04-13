package com.neogamin.proyecto_formativo.compartido.aplicacion;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
