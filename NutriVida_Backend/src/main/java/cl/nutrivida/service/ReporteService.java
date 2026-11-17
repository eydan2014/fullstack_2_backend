package cl.nutrivida.service;

import cl.nutrivida.dto.ReporteAtenciones;
import cl.nutrivida.dto.ReporteAtenciones.Profesional;
import cl.nutrivida.repository.ConsultaCitasRepository;
import cl.nutrivida.repository.ConsultaCitasRepository.Conteo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReporteService {
    private final ConsultaCitasRepository citas;

    public ReporteService(ConsultaCitasRepository citas) {
        this.citas = citas;
    }

    public ReporteAtenciones atenciones(LocalDate desde, LocalDate hasta, String profesionalId) {
        LocalDate fin = hasta == null ? LocalDate.now(ZoneId.of("America/Santiago")) : hasta;
        LocalDate inicio = desde == null ? fin.withDayOfMonth(1) : desde;
        if (inicio.isAfter(fin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha desde debe ser anterior o igual a hasta.");
        }

        String id = profesionalId == null || profesionalId.isBlank() ? null : profesionalId.strip();
        Map<String, Long> porEstado = new LinkedHashMap<>();
        Map<String, Profesional> porProfesional = new LinkedHashMap<>();
        long total = 0;
        for (Conteo fila : consultar(inicio, fin, id)) {
            total += fila.cantidad();
            porEstado.merge(fila.estado(), fila.cantidad(), Long::sum);
            Profesional profesional = new Profesional(fila.profesionalId(), fila.nombre(), fila.cantidad());
            porProfesional.merge(fila.profesionalId(), profesional,
                    (a, b) -> new Profesional(a.id(), a.nombre(), a.total() + b.total()));
        }

        long atendidas = porEstado.getOrDefault("ATENDIDA", 0L);
        long inasistencias = porEstado.getOrDefault("INASISTENCIA", 0L);
        long cerradas = atendidas + inasistencias;
        BigDecimal tasa = cerradas == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(inasistencias)
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(cerradas), 2, RoundingMode.HALF_UP);

        return new ReporteAtenciones(inicio, fin, total, atendidas, inasistencias,
                porEstado.getOrDefault("CANCELADA", 0L), tasa, porEstado, List.copyOf(porProfesional.values()));
    }

    private List<Conteo> consultar(LocalDate desde, LocalDate hasta, String profesionalId) {
        try {
            return citas.contarAtenciones(desde, hasta, profesionalId);
        } catch (DataAccessException error) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudieron consultar las citas para generar el reporte.", error);
        }
    }
}
