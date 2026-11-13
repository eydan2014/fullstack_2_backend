package cl.nutrivida.repository;

import cl.nutrivida.model.Servicio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, String> {
    List<Servicio> findByActivoTrueOrderByNombreAscIdAsc();
}
