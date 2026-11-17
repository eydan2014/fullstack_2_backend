package cl.nutrivida.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ReporteAtenciones(
        LocalDate desde,
        LocalDate hasta,
        long total,
        long atendidas,
        long inasistencias,
        long canceladas,
        BigDecimal tasaInasistencia,
        Map<String, Long> porEstado,
        List<Profesional> porProfesional) {

    public record Profesional(String id, String nombre, long total) {}
}
