package com.umg.quejasbancario.repository;

import com.umg.quejasbancario.entity.Usuario;
import com.umg.quejasbancario.entity.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);
    Optional<Usuario> findByCorreoElectronicoIgnoreCase(String correo);
    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);
    boolean existsByCorreoElectronicoIgnoreCase(String correo);

    List<Usuario> findByRol_NombreRolIgnoreCaseAndEstado(String nombreRol, EstadoUsuario estado);
    List<Usuario> findByRol_NombreRolIgnoreCase(String nombreRol);
}
