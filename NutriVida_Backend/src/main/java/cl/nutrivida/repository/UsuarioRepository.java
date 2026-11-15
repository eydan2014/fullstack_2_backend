package cl.nutrivida.repository;

import cl.nutrivida.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    boolean existsByCorreoOrRut(String correo, String rut);
    Optional<Usuario> findByCorreo(String correo);
}
