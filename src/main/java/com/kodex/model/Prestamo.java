package com.kodex.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entidad Prestamo (SDD-06, SDD-07, SDD-08, SDD-10, SDD-11, SDD-18).
 * Conecta Lector, Ejemplar y Usuario (bibliotecario/admin que registra).
 */
@Entity
@Table(name = "prestamo")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long idPrestamo;

    @NotNull(message = "La fecha de préstamo es obligatoria")
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;

    @NotNull(message = "La fecha límite es obligatoria")
    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Min(value = 0)
    @Column(name = "renovaciones", nullable = false)
    private Integer renovaciones = 0;

    @NotNull(message = "El estado del préstamo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @NotNull(message = "El lector es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_lector", nullable = false)
    private Lector lector;

    @NotNull(message = "El ejemplar es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_ejemplar", nullable = false)
    private Ejemplar ejemplar;

    @NotNull(message = "El usuario responsable es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToOne(mappedBy = "prestamo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Multa multa;

    public Prestamo() {
    }

    public Prestamo(LocalDate fechaPrestamo, LocalDate fechaLimite, Lector lector, Ejemplar ejemplar, Usuario usuario) {
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.lector = lector;
        this.ejemplar = ejemplar;
        this.usuario = usuario;
        this.renovaciones = 0;
        this.estado = EstadoPrestamo.ACTIVO;
    }

    public Long getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public Integer getRenovaciones() {
        return renovaciones;
    }

    public void setRenovaciones(Integer renovaciones) {
        this.renovaciones = renovaciones;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public Lector getLector() {
        return lector;
    }

    public void setLector(Lector lector) {
        this.lector = lector;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(Ejemplar ejemplar) {
        this.ejemplar = ejemplar;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Multa getMulta() {
        return multa;
    }

    public void setMulta(Multa multa) {
        this.multa = multa;
    }

    /**
     * Determina si el préstamo está vencido en relación a hoy (o a la fecha de devolución).
     */
    public boolean isVencido() {
        if (EstadoPrestamo.DEVUELTO.equals(this.estado)) {
            return fechaDevolucion != null && fechaDevolucion.isAfter(fechaLimite);
        }
        return LocalDate.now().isAfter(fechaLimite);
    }
}
