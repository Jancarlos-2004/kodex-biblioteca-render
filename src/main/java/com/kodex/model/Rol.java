package com.kodex.model;

/**
 * Roles del sistema KODEX definidos en la especificación SDD.
 * Tabla 1 de KODEX.pdf:
 * 1. ADMIN (Administrador general)
 * 2. BIBLIOTECARIO
 * 3. CATALOGADOR
 * 4. TESORERO
 * 5. GERENTE
 * 6. LECTOR
 */
public enum Rol {
    ADMIN("Administrador General"),
    BIBLIOTECARIO("Bibliotecario"),
    CATALOGADOR("Catalogador"),
    TESORERO("Tesorero"),
    GERENTE("Gerente"),
    LECTOR("Lector");

    private final String nombreVisible;

    Rol(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
