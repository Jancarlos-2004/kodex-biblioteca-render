package com.kodex.repository;

import com.kodex.model.Lector;
import com.kodex.model.Rol;
import com.kodex.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdUsuarioNot(String correo, Long idUsuario);

    List<Usuario> findByRol(Rol rol);

    Optional<Usuario> findByLector(Lector lector);

    List<Usuario> findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(String nombre, String correo);
}
