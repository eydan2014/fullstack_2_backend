package cl.nutrivida.repository;

import cl.nutrivida.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    boolean existsByCorreoOrRut(String correo, String rut);
}
