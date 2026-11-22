package com.kodex.repository;

import com.kodex.model.EstadoMulta;
import com.kodex.model.Multa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long> {

    List<Multa> findByEstado(EstadoMulta estado);

    List<Multa> findByPrestamoLectorIdLectorOrderByPrestamoFechaPrestamoDesc(Long idLector);

    List<Multa> findByPrestamoLectorIdLectorAndEstado(Long idLector, EstadoMulta estado);

    long countByPrestamoLectorIdLectorAndEstado(Long idLector, EstadoMulta estado);

    long countByEstado(EstadoMulta estado);

    Optional<Multa> findByPrestamoIdPrestamo(Long idPrestamo);

    @Query("SELECT COALESCE(SUM(m.valor), 0.0) FROM Multa m WHERE m.estado = com.kodex.model.EstadoMulta.PAGADA")
    BigDecimal sumTotalRecaudado();

    @Query("SELECT COALESCE(SUM(m.valor), 0.0) FROM Multa m WHERE m.estado = com.kodex.model.EstadoMulta.PENDIENTE")
    BigDecimal sumTotalPendiente();

    @Query("SELECT m FROM Multa m WHERE " +
           "(:estado IS NULL OR m.estado = :estado) AND " +
           "(:desde IS NULL OR m.fechaPago >= :desde) AND " +
           "(:hasta IS NULL OR m.fechaPago <= :hasta) " +
           "ORDER BY m.idMulta DESC")
    List<Multa> filtrarMultas(@Param("estado") EstadoMulta estado,
                              @Param("desde") LocalDate desde,
                              @Param("hasta") LocalDate hasta);

    @Query("SELECT COALESCE(SUM(m.valor), 0.0) FROM Multa m WHERE " +
           "m.estado = com.kodex.model.EstadoMulta.PAGADA AND " +
           "(:desde IS NULL OR m.fechaPago >= :desde) AND " +
           "(:hasta IS NULL OR m.fechaPago <= :hasta)")
    BigDecimal sumRecaudadoPorPeriodo(@Param("desde") LocalDate desde,
                                      @Param("hasta") LocalDate hasta);
}
