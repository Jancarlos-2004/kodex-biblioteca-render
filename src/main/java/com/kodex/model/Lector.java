package com.kodex.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Lector (SDD-05, SDD-08, SDD-09).
 * Representa a los usuarios que solicitan préstamos y reservas.
 */
@Entity
@Table(name = "lector")
public class Lector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lector")
    private Long idLector;

    @NotBlank(message = "El documento de identidad es obligatorio")
    @Size(max = 30, message = "El documento no puede exceder 30 caracteres")
    @Column(name = "documento", nullable = false, unique = true, length = 30)
    private String documento;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
    @Column(name = "telefono", length = 30)
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Ingrese un correo electrónico válido")
    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @NotNull(message = "El estado del lector es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoLector estado = EstadoLector.ACTIVO;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "lector", fetch = FetchType.LAZY)
    private List<Prestamo> prestamos = new ArrayList<>();

    @OneToMany(mappedBy = "lector", fetch = FetchType.LAZY)
    private List<ReservaLibro> reservas = new ArrayList<>();

    public Lector() {
    }

    public Lector(String documento, String nombre, String telefono, String correo) {
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = EstadoLector.ACTIVO;
        this.fechaRegistro = LocalDateTime.now();
    }

    public Long getIdLector() {
        return idLector;
    }

    public void setIdLector(Long idLector) {
        this.idLector = idLector;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public EstadoLector getEstado() {
        return estado;
    }

    public void setEstado(EstadoLector estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<Prestamo> prestamos) {
        this.prestamos = prestamos;
    }

    public List<ReservaLibro> getReservas() {
        return reservas;
    }

    public void setReservas(List<ReservaLibro> reservas) {
        this.reservas = reservas;
    }
}
