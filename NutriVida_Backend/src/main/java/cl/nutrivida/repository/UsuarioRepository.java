package cl.nutrivida.repository;

import cl.nutrivida.model.Usuario;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByRut(String rut);
    boolean existsByRol(String rol);
    List<Usuario> findAllByOrderByNombreAscCorreoAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id in :ids order by u.id")
    List<Usuario> bloquearPorIds(@Param("ids") List<String> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.correo = :correo")
    Optional<Usuario> bloquearPorCorreo(@Param("correo") String correo);
}
