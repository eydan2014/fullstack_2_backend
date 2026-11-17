package cl.nutrivida.repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultaCitasRepository {
    private final JdbcTemplate jdbc;

    public ConsultaCitasRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean tieneCitasActivas(String servicioId) {
        try {
            Boolean existen = jdbc.queryForObject("""
                    SELECT EXISTS (
                        SELECT 1 FROM citas
                        WHERE servicio_id = ?
                          AND (estado IS NULL OR UPPER(TRIM(estado)) NOT IN ('CANCELADA', 'ATENDIDA', 'INASISTENCIA'))
                    )
                    """, Boolean.class, servicioId);
            return Boolean.TRUE.equals(existen);
        } catch (BadSqlGrammarException error) {
            SQLException sql = error.getSQLException();
            if (sql != null && "42S02".equals(sql.getSQLState())) {
                return false;
            }
            throw error;
        }
    }

    public List<Conteo> contarAtenciones(LocalDate desde, LocalDate hasta, String profesionalId) {
        String filtro = profesionalId == null ? "" : " AND c.profesional_id = ?";
        Object[] parametros = profesionalId == null
                ? new Object[]{desde, hasta}
                : new Object[]{desde, hasta, profesionalId};
        return jdbc.query("""
                SELECT c.profesional_id, COALESCE(u.nombre, c.profesional_id, 'Sin asignar') AS nombre,
                       UPPER(COALESCE(NULLIF(TRIM(c.estado), ''), 'SIN_ESTADO')) AS estado_reporte,
                       COUNT(*) AS cantidad
                FROM citas c
                LEFT JOIN usuarios u ON u.id = c.profesional_id
                WHERE c.fecha BETWEEN ? AND ?
                """ + filtro + """

                GROUP BY c.profesional_id, u.nombre, estado_reporte
                ORDER BY c.profesional_id, estado_reporte
                """, (fila, numero) -> new Conteo(fila.getString("profesional_id"),
                fila.getString("nombre"), fila.getString("estado_reporte"), fila.getLong("cantidad")),
                parametros);
    }

    public record Conteo(String profesionalId, String nombre, String estado, long cantidad) {}
}
