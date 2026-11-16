package cl.nutrivida.repository;

import java.sql.SQLException;
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
}
