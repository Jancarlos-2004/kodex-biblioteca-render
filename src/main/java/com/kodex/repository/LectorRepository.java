package com.kodex.repository;

import com.kodex.model.EstadoLector;
import com.kodex.model.Lector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectorRepository extends JpaRepository<Lector, Long> {

    Optional<Lector> findByDocumento(String documento);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdLectorNot(String documento, Long idLector);

    List<Lector> findByEstado(EstadoLector estado);

    List<Lector> findByNombreContainingIgnoreCaseOrDocumentoContaining(String nombre, String documento);
}
