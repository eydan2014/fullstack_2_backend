package cl.nutrivida.service;

import cl.nutrivida.model.Servicio;
import cl.nutrivida.repository.ServicioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {
    private final ServicioRepository repositorio;

    public ServicioService(ServicioRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Servicio> listar() {
        return repositorio.findByActivoTrueOrderByNombreAscIdAsc();
    }

    public Optional<Servicio> buscar(String id) {
        return repositorio.findById(id).filter(Servicio::isActivo);
    }
}
