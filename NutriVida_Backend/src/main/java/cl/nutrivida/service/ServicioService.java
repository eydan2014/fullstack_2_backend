package cl.nutrivida.service;

import cl.nutrivida.model.Servicio;
import cl.nutrivida.repository.ServicioRepository;
import java.util.List;
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
}
