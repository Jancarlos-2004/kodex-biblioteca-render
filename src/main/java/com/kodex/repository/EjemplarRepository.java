package com.kodex.repository;

import com.kodex.model.Ejemplar;
import com.kodex.model.EstadoEjemplar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {

    Optional<Ejemplar> findByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventarioAndIdEjemplarNot(String codigoInventario, Long idEjemplar);

    List<Ejemplar> findByLibroIdLibro(Long idLibro);

    List<Ejemplar> findByEstado(EstadoEjemplar estado);

    List<Ejemplar> findByLibroIdLibroAndEstado(Long idLibro, EstadoEjemplar estado);

    long countByLibroIdLibroAndEstado(Long idLibro, EstadoEjemplar estado);

    long countByEstado(EstadoEjemplar estado);

    @Query("SELECT e FROM Ejemplar e WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(e.codigoInventario) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.libro.titulo) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:estado IS NULL OR e.estado = :estado)")
    List<Ejemplar> filtrarEjemplares(@Param("query") String query, @Param("estado") EstadoEjemplar estado);
}
