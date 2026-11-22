package com.kodex.model;

/**
 * Estados de un préstamo según SDD-06, SDD-07 y SDD-11.
 */
public enum EstadoPrestamo {
    ACTIVO("Activo"),
    DEVUELTO("Devuelto"),
    VENCIDO("Vencido");

    private final String nombreVisible;

    EstadoPrestamo(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
