package cl.nutrivida.service;

import cl.nutrivida.config.SeguridadConfig;
import cl.nutrivida.dto.LoginRespuesta;
import cl.nutrivida.dto.LoginSolicitud;
import cl.nutrivida.model.Usuario;
import cl.nutrivida.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InicioSesionService {
    private static final Duration VIGENCIA = Duration.ofMinutes(15);
    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final JwtEncoder emisor;
    private final String hashSinCuenta;

    public InicioSesionService(UsuarioRepository usuarios, PasswordEncoder codificador, JwtEncoder emisor) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emisor = emisor;
        this.hashSinCuenta = codificador.encode(UUID.randomUUID().toString());
    }

    public LoginRespuesta iniciar(LoginSolicitud datos) {
        if (datos.clave().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave supera el limite de 72 bytes.");
        }
        Usuario usuario = usuarios.findByCorreo(datos.correo()).filter(Usuario::isActivo).orElse(null);
        String hash = usuario == null ? hashSinCuenta : usuario.getClaveHash();
        boolean coincide = codificador.matches(datos.clave(), hash);
        if (!coincide || usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o clave incorrectos.");
        }

        Instant ahora = Instant.now();
        JwtClaimsSet datosToken = JwtClaimsSet.builder()
                .issuer(SeguridadConfig.EMISOR)
                .audience(List.of(SeguridadConfig.AUDIENCIA))
                .subject(usuario.getId())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(VIGENCIA))
                .claim("rol", usuario.getRol())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = emisor.encode(JwtEncoderParameters.from(cabecera, datosToken)).getTokenValue();

        LoginRespuesta.UsuarioSesion cuenta = new LoginRespuesta.UsuarioSesion(
                usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
        return new LoginRespuesta(token, "Bearer", VIGENCIA.toSeconds(), cuenta);
    }
}
