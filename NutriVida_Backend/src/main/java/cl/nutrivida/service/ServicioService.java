package cl.nutrivida.service;

import cl.nutrivida.dto.DatosServicio;
import cl.nutrivida.model.Servicio;
import cl.nutrivida.repository.ConsultaCitasRepository;
import cl.nutrivida.repository.ServicioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ServicioService {
    private final ServicioRepository repositorio;
    private final ConsultaCitasRepository citas;

    @PersistenceContext
    private EntityManager entidades;

    public ServicioService(ServicioRepository repositorio, ConsultaCitasRepository citas) {
        this.repositorio = repositorio;
        this.citas = citas;
    }

    public List<Servicio> listar() {
        return repositorio.findByActivoTrueOrderByNombreAscIdAsc();
    }

    public Optional<Servicio> buscar(String id) {
        return repositorio.findById(normalizarCodigo(id)).filter(Servicio::isActivo);
    }

    public List<Servicio> listarAdministracion() {
        return repositorio.findAllByOrderByNombreAscIdAsc();
    }

    @Transactional
    public Servicio crear(DatosServicio datos) {
        if (datos.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica un codigo con el formato AA000.");
        }
        if (repositorio.existsById(datos.id())) {
            throw codigoDuplicado();
        }
        Servicio nuevo = new Servicio(datos.id(), datos.nombre(), datos.descripcion(), datos.tipo(),
                datos.modalidad(), datos.precio(), datos.duracionMinutos());
        try {
            entidades.persist(nuevo);
            entidades.flush();
        } catch (EntityExistsException | ConstraintViolationException error) {
            throw codigoDuplicado();
        }
        return nuevo;
    }

    @Transactional
    public Servicio editar(String id, DatosServicio datos) {
        String codigo = normalizarCodigo(id);
        if (datos.id() != null && !codigo.equals(datos.id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El codigo del servicio no se puede cambiar.");
        }
        Servicio servicio = bloquear(codigo);
        servicio.actualizar(datos.nombre(), datos.descripcion(), datos.tipo(), datos.modalidad(),
                datos.precio(), datos.duracionMinutos());
        return servicio;
    }

    @Transactional
    public Servicio cambiarEstado(String id, boolean activo) {
        Servicio servicio = bloquear(normalizarCodigo(id));
        if (!activo && servicio.isActivo()) {
            comprobarBaja(servicio.getId());
        }
        servicio.cambiarEstado(activo);
        return servicio;
    }

    private void comprobarBaja(String id) {
        boolean hayCitasActivas;
        try {
            hayCitasActivas = citas.tieneCitasActivas(id);
        } catch (DataAccessException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo comprobar las citas del servicio.", error);
        }
        if (hayCitasActivas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El servicio tiene citas activas. Cancela o atiende esas citas primero.");
        }
    }

    private Servicio bloquear(String id) {
        return repositorio.bloquear(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado."));
    }

    private String normalizarCodigo(String id) {
        return id.strip().toUpperCase(Locale.ROOT);
    }

    private ResponseStatusException codigoDuplicado() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El codigo del servicio ya existe.");
    }
}
