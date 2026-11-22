package com.kodex.model;

/**
 * Estados del lector según SDD-05 y reglas de negocio.
 */
public enum EstadoLector {
    ACTIVO("Activo"),
    SANCIONADO("Sancionado");

    private final String nombreVisible;

    EstadoLector(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
