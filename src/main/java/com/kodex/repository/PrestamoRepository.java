package com.kodex.repository;

import com.kodex.model.EstadoPrestamo;
import com.kodex.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByLectorIdLectorOrderByFechaPrestamoDesc(Long idLector);

    List<Prestamo> findByLectorIdLectorAndEstado(Long idLector, EstadoPrestamo estado);

    long countByLectorIdLectorAndEstado(Long idLector, EstadoPrestamo estado);

    List<Prestamo> findByEjemplarIdEjemplarAndEstadoIn(Long idEjemplar, Collection<EstadoPrestamo> estados);

    long countByEstado(EstadoPrestamo estado);

    List<Prestamo> findByEstadoOrderByFechaLimiteAsc(EstadoPrestamo estado);

    @Query("SELECT p FROM Prestamo p WHERE " +
           "(:idLector IS NULL OR p.lector.idLector = :idLector) AND " +
           "(:estado IS NULL OR p.estado = :estado) AND " +
           "(:desde IS NULL OR p.fechaPrestamo >= :desde) AND " +
           "(:hasta IS NULL OR p.fechaPrestamo <= :hasta) " +
           "ORDER BY p.fechaPrestamo DESC")
    List<Prestamo> filtrarPrestamos(@Param("idLector") Long idLector,
                                    @Param("estado") EstadoPrestamo estado,
                                    @Param("desde") LocalDate desde,
                                    @Param("hasta") LocalDate hasta);

    @Query("SELECT p FROM Prestamo p WHERE p.estado = com.kodex.model.EstadoPrestamo.ACTIVO AND p.fechaLimite < CURRENT_DATE")
    List<Prestamo> findPrestamosVencidos();
}
