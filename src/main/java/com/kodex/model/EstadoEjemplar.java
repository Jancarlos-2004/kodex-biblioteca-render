package com.kodex.model;

/**
 * Estados del ejemplar físico según SDD-03 y SDD-04.
 */
public enum EstadoEjemplar {
    DISPONIBLE("Disponible"),
    PRESTADO("Prestado"),
    RESERVADO("Reservado"),
    EN_REPARACION("En Reparación"),
    BAJA("Dado de Baja");

    private final String nombreVisible;

    EstadoEjemplar(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
