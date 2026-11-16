package cl.nutrivida.repository;

import cl.nutrivida.model.Servicio;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicioRepository extends JpaRepository<Servicio, String> {
    List<Servicio> findByActivoTrueOrderByNombreAscIdAsc();
    List<Servicio> findAllByOrderByNombreAscIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Servicio s where s.id = :id")
    Optional<Servicio> bloquear(@Param("id") String id);
}
