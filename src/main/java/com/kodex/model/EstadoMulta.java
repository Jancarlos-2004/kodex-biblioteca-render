package com.kodex.model;

/**
 * Estados de una multa según SDD-12, SDD-13 y SDD-14.
 */
public enum EstadoMulta {
    PENDIENTE("Pendiente"),
    PAGADA("Pagada");

    private final String nombreVisible;

    EstadoMulta(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
