package com.kodex.repository;

import com.kodex.model.EstadoReserva;
import com.kodex.model.ReservaLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaLibroRepository extends JpaRepository<ReservaLibro, Long> {

    List<ReservaLibro> findByLectorIdLectorOrderByFechaSolicitudDesc(Long idLector);

    List<ReservaLibro> findByLibroIdLibroAndEstado(Long idLibro, EstadoReserva estado);

    long countByLibroIdLibroAndEstado(Long idLibro, EstadoReserva estado);

    long countByEstado(EstadoReserva estado);

    List<ReservaLibro> findByEstadoOrderByFechaSolicitudAsc(EstadoReserva estado);

    boolean existsByLectorIdLectorAndLibroIdLibroAndEstado(Long idLector, Long idLibro, EstadoReserva estado);

    @Query("SELECT r FROM ReservaLibro r WHERE " +
           "(:idLibro IS NULL OR r.libro.idLibro = :idLibro) AND " +
           "(:idLector IS NULL OR r.lector.idLector = :idLector) AND " +
           "(:estado IS NULL OR r.estado = :estado) " +
           "ORDER BY r.fechaSolicitud DESC")
    List<ReservaLibro> filtrarReservas(@Param("idLibro") Long idLibro,
                                       @Param("idLector") Long idLector,
                                       @Param("estado") EstadoReserva estado);

    @Query("SELECT r FROM ReservaLibro r WHERE r.estado = com.kodex.model.EstadoReserva.ACTIVA AND r.fechaVencimiento < CURRENT_DATE")
    List<ReservaLibro> findReservasVencidas();
}
