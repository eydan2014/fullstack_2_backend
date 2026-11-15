package cl.nutrivida.repository;

import cl.nutrivida.model.TokenRenovacion;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRenovacionRepository extends JpaRepository<TokenRenovacion, String> {
    @Query("select t.sesion.id from TokenRenovacion t where t.huella = :huella")
    Optional<String> buscarSesionId(@Param("huella") String huella);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TokenRenovacion> findByHuella(String huella);
}
