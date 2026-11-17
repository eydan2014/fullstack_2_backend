package cl.nutrivida.config;

import cl.nutrivida.model.Sesion;
import cl.nutrivida.repository.SesionRepository;
import jakarta.servlet.DispatcherType;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {
    public static final String EMISOR = "nutrivida";
    public static final String AUDIENCIA = "nutrivida-api";

    @Bean
    PasswordEncoder codificador() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecretKey claveJwt(@Value("${nutrivida.jwt.clave}") String claveConfigurada) {
        byte[] bytes = new byte[32];
        if (claveConfigurada.isBlank()) {
            new SecureRandom().nextBytes(bytes);
        } else {
            try {
                bytes = Base64.getDecoder().decode(claveConfigurada.strip());
            } catch (IllegalArgumentException error) {
                throw new IllegalStateException("NUTRIVIDA_JWT_CLAVE debe estar en Base64.");
            }
            if (bytes.length < 32) {
                throw new IllegalStateException("NUTRIVIDA_JWT_CLAVE debe contener al menos 32 bytes.");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey claveJwt) {
        return NimbusJwtEncoder.withSecretKey(claveJwt).algorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey claveJwt) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withSecretKey(claveJwt)
                .macAlgorithm(MacAlgorithm.HS256).build();
        decodificador.setJwtValidator(JwtValidators.createDefaultWithValidators(List.of(
                new JwtTimestampValidator(Duration.ZERO),
                new JwtIssuerValidator(EMISOR),
                new JwtAudienceValidator(AUDIENCIA),
                new JwtClaimValidator<Instant>(JwtClaimNames.EXP, Objects::nonNull),
                new JwtClaimValidator<String>(JwtClaimNames.SUB, id -> id != null && !id.isBlank()),
                new JwtClaimValidator<String>("sid", id -> id != null && !id.isBlank()))));
        return decodificador;
    }

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http, SesionRepository sesiones) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/servicios", "/api/v1/servicios/*").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/api/v1/servicios", "/api/v1/servicios/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/servicios").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/atenciones").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/servicios").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/servicios/*").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/servicios/*/estado").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/servicios/*").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/registro", "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/sesion").hasRole("PACIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios", "/api/v1/usuarios/*").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/*").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/usuarios/*/estado").hasRole("ADMINISTRADOR")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(api -> api.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(token -> autenticar(token, sesiones))))
                .build();
    }

    private JwtAuthenticationToken autenticar(Jwt token, SesionRepository sesiones) {
        Sesion sesion = sesiones.findById(token.getClaimAsString("sid"))
                .filter(cuenta -> cuenta.vigente(Instant.now()))
                .filter(cuenta -> cuenta.getUsuario().getId().equals(token.getSubject())
                        && cuenta.getRol().equals(token.getClaimAsString("rol")))
                .orElseThrow(() -> new InvalidBearerTokenException("Sesion no valida."));
        return new JwtAuthenticationToken(token, List.of(new SimpleGrantedAuthority("ROLE_" + sesion.getRol())));
    }
}
