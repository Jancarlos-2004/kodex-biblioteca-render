package com.kodex.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Parámetros del sistema configurables por el Administrador General.
 * Permite ajustar plazo de préstamo, multas y límites sin recompilar.
 */
@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El plazo de préstamo en días es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es de 1 día")
    @Column(name = "dias_prestamo", nullable = false)
    private Integer diasPrestamo = 7;

    @NotNull(message = "El límite de préstamos activos es obligatorio")
    @Min(value = 1, message = "El límite mínimo es de 1 libro")
    @Column(name = "max_prestamos_activos", nullable = false)
    private Integer maxPrestamosActivos = 3;

    @NotNull(message = "El valor de la multa diaria es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "La multa diaria debe ser mayor a 0")
    @Column(name = "valor_multa_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorMultaDiaria = new BigDecimal("1000.00");

    @NotNull(message = "Los días de vigencia de reserva son obligatorios")
    @Min(value = 1, message = "La vigencia mínima es de 1 día")
    @Column(name = "dias_vencimiento_reserva", nullable = false)
    private Integer diasVencimientoReserva = 5;

    @NotBlank(message = "El nombre de la biblioteca es obligatorio")
    @Column(name = "nombre_biblioteca", nullable = false, length = 150)
    private String nombreBiblioteca = "Biblioteca KODEX";

    public Configuracion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDiasPrestamo() {
        return diasPrestamo;
    }

    public void setDiasPrestamo(Integer diasPrestamo) {
        this.diasPrestamo = diasPrestamo;
    }

    public Integer getMaxPrestamosActivos() {
        return maxPrestamosActivos;
    }

    public void setMaxPrestamosActivos(Integer maxPrestamosActivos) {
        this.maxPrestamosActivos = maxPrestamosActivos;
    }

    public BigDecimal getValorMultaDiaria() {
        return valorMultaDiaria;
    }

    public void setValorMultaDiaria(BigDecimal valorMultaDiaria) {
        this.valorMultaDiaria = valorMultaDiaria;
    }

    public Integer getDiasVencimientoReserva() {
        return diasVencimientoReserva;
    }

    public void setDiasVencimientoReserva(Integer diasVencimientoReserva) {
        this.diasVencimientoReserva = diasVencimientoReserva;
    }

    public String getNombreBiblioteca() {
        return nombreBiblioteca;
    }

    public void setNombreBiblioteca(String nombreBiblioteca) {
        this.nombreBiblioteca = nombreBiblioteca;
    }
}
