package com.kodex.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad Multa (SDD-12, SDD-13, SDD-14, SDD-19).
 * Originada por devolución tardía del ejemplar.
 */
@Entity
@Table(name = "multa")
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_multa")
    private Long idMulta;

    @Min(value = 0, message = "Los días de retraso no pueden ser negativos")
    @Column(name = "dias_retraso", nullable = false)
    private Integer diasRetraso;

    @NotNull(message = "El valor de la multa es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El valor de la multa debe ser positivo")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @NotNull(message = "El estado de la multa es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoMulta estado = EstadoMulta.PENDIENTE;

    @NotNull(message = "El préstamo asociado es obligatorio")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_prestamo", nullable = false, unique = true)
    private Prestamo prestamo;

    public Multa() {
    }

    public Multa(Integer diasRetraso, BigDecimal valor, Prestamo prestamo) {
        this.diasRetraso = diasRetraso;
        this.valor = valor;
        this.prestamo = prestamo;
        this.estado = EstadoMulta.PENDIENTE;
    }

    public Long getIdMulta() {
        return idMulta;
    }

    public void setIdMulta(Long idMulta) {
        this.idMulta = idMulta;
    }

    public Integer getDiasRetraso() {
        return diasRetraso;
    }

    public void setDiasRetraso(Integer diasRetraso) {
        this.diasRetraso = diasRetraso;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public EstadoMulta getEstado() {
        return estado;
    }

    public void setEstado(EstadoMulta estado) {
        this.estado = estado;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }
}
