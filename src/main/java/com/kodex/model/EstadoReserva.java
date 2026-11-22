package com.kodex.model;

/**
 * Estados de una reserva de libro según SDD-16 y SDD-17.
 */
public enum EstadoReserva {
    ACTIVA("Activa"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada"),
    EXPIRADA("Expirada");

    private final String nombreVisible;

    EstadoReserva(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
