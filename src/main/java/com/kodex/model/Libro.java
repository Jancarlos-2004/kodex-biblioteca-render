package com.kodex.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Libro (SDD-02, SDD-15).
 * Representa el título o catálogo bibliográfico (no la copia física individual).
 */
@Entity
@Table(name = "libro")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long idLibro;

    @NotBlank(message = "El código ISBN es obligatorio")
    @Size(max = 30, message = "El ISBN no puede exceder 30 caracteres")
    @Column(name = "isbn", nullable = false, unique = true, length = 30)
    private String isbn;

    @NotBlank(message = "El título del libro es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 150, message = "El autor no puede exceder 150 caracteres")
    @Column(name = "autor", nullable = false, length = 150)
    private String autor;

    @NotBlank(message = "La editorial es obligatoria")
    @Size(max = 100, message = "La editorial no puede exceder 100 caracteres")
    @Column(name = "editorial", nullable = false, length = 100)
    private String editorial;

    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1000, message = "El año debe ser válido (mayor a 1000)")
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ejemplar> ejemplares = new ArrayList<>();

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReservaLibro> reservas = new ArrayList<>();

    public Libro() {
    }

    public Libro(String isbn, String titulo, String autor, String editorial, Integer anio, Categoria categoria) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anio = anio;
        this.categoria = categoria;
    }

    public Long getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Long idLibro) {
        this.idLibro = idLibro;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Ejemplar> getEjemplares() {
        return ejemplares;
    }

    public void setEjemplares(List<Ejemplar> ejemplares) {
        this.ejemplares = ejemplares;
    }

    public List<ReservaLibro> getReservas() {
        return reservas;
    }

    public void setReservas(List<ReservaLibro> reservas) {
        this.reservas = reservas;
    }

    /**
     * Devuelve el total de copias físicas registradas para este libro.
     */
    public long getTotalEjemplares() {
        return ejemplares != null ? ejemplares.size() : 0;
    }

    /**
     * Devuelve la cantidad de ejemplares actualmente en estado DISPONIBLE.
     */
    public long getEjemplaresDisponibles() {
        if (ejemplares == null) return 0;
        return ejemplares.stream()
                .filter(e -> EstadoEjemplar.DISPONIBLE.equals(e.getEstado()))
                .count();
    }
}
