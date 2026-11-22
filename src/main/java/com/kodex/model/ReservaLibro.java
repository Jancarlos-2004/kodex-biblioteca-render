package com.kodex.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entidad ReservaLibro (SDD-16, SDD-17).
 * Se solicita sobre un título (Libro) cuando no existen ejemplares físicos disponibles.
 */
@Entity
@Table(name = "reserva_libro")
public class ReservaLibro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long idReserva;

    @NotNull(message = "La fecha de solicitud es obligatoria")
    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @NotNull(message = "El estado de la reserva es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    @NotNull(message = "El lector es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_lector", nullable = false)
    private Lector lector;

    @NotNull(message = "El libro es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_libro", nullable = false)
    private Libro libro;

    public ReservaLibro() {
    }

    public ReservaLibro(LocalDate fechaSolicitud, LocalDate fechaVencimiento, Lector lector, Libro libro) {
        this.fechaSolicitud = fechaSolicitud;
        this.fechaVencimiento = fechaVencimiento;
        this.lector = lector;
        this.libro = libro;
        this.estado = EstadoReserva.ACTIVA;
    }

    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Long idReserva) {
        this.idReserva = idReserva;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Lector getLector() {
        return lector;
    }

    public void setLector(Lector lector) {
        this.lector = lector;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public boolean isExpirada() {
        return EstadoReserva.ACTIVA.equals(this.estado) && LocalDate.now().isAfter(fechaVencimiento);
    }
}
