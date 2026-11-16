package cl.nutrivida.config;

import cl.nutrivida.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class UsuariosIniciales {
    @Bean
    ApplicationRunner prepararUsuarios(UsuarioService usuarios, JdbcTemplate jdbc, Environment entorno,
                                       @Value("${nutrivida.admin.correo}") String correo,
                                       @Value("${nutrivida.admin.clave}") String clave) {
        return argumentos -> {
            if (entorno.matchesProfiles("mysql")) {
                Integer obligatorias = jdbc.queryForObject("""
                        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuarios'
                          AND COLUMN_NAME IN ('rut', 'telefono') AND IS_NULLABLE = 'NO'
                        """, Integer.class);
                if (obligatorias != null && obligatorias > 0) {
                    jdbc.execute("ALTER TABLE usuarios MODIFY COLUMN rut VARCHAR(10) NULL, "
                            + "MODIFY COLUMN telefono VARCHAR(13) NULL");
                }
            }
            usuarios.prepararAdministrador(correo, clave);
        };
    }
}
