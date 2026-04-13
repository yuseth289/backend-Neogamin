package com.neogamin.proyecto_formativo.compartido.aplicacion;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
