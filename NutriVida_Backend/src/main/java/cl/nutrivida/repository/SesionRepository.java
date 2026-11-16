package cl.nutrivida.repository;

import cl.nutrivida.model.Sesion;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SesionRepository extends JpaRepository<Sesion, String> {
    @Override
    @EntityGraph(attributePaths = "usuario")
    Optional<Sesion> findById(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sesion s where s.id = :id")
    Optional<Sesion> bloquear(@Param("id") String id);

    @Modifying(flushAutomatically = true)
    @Query("update Sesion s set s.activa = false where s.usuario.id = :id and s.activa = true")
    void revocarDeUsuario(@Param("id") String id);
}
