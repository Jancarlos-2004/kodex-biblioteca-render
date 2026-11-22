package com.kodex.repository;

import com.kodex.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdLibroNot(String isbn, Long idLibro);

    List<Libro> findByCategoriaIdCategoria(Long idCategoria);

    @Query("SELECT l FROM Libro l WHERE " +
           "LOWER(l.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.autor) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.isbn) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Libro> buscarPorCriterio(@Param("query") String query);

    @Query("SELECT l FROM Libro l WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:idCategoria IS NULL OR l.categoria.idCategoria = :idCategoria)")
    List<Libro> filtrarCatalogo(@Param("query") String query, @Param("idCategoria") Long idCategoria);

    @Query("SELECT p.ejemplar.libro.idLibro, p.ejemplar.libro.titulo, p.ejemplar.libro.autor, COUNT(p.idPrestamo) AS totalPrestamos " +
           "FROM Prestamo p GROUP BY p.ejemplar.libro.idLibro, p.ejemplar.libro.titulo, p.ejemplar.libro.autor " +
           "ORDER BY COUNT(p.idPrestamo) DESC")
    List<Object[]> findLibrosMasPrestados();
}
