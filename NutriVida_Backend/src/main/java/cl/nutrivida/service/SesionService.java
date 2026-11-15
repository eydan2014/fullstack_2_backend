package cl.nutrivida.service;

import cl.nutrivida.config.SeguridadConfig;
import cl.nutrivida.dto.LoginRespuesta;
import cl.nutrivida.model.Sesion;
import cl.nutrivida.model.TokenRenovacion;
import cl.nutrivida.model.Usuario;
import cl.nutrivida.repository.SesionRepository;
import cl.nutrivida.repository.TokenRenovacionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SesionService {
    private static final Duration VIGENCIA_ACCESO = Duration.ofMinutes(15);
    private static final Duration VIGENCIA_SESION = Duration.ofDays(7);
    private final SesionRepository sesiones;
    private final TokenRenovacionRepository renovaciones;
    private final JwtEncoder emisor;
    private final SecureRandom aleatorio = new SecureRandom();

    public SesionService(SesionRepository sesiones, TokenRenovacionRepository renovaciones, JwtEncoder emisor) {
        this.sesiones = sesiones;
        this.renovaciones = renovaciones;
        this.emisor = emisor;
    }

    @Transactional
    public LoginRespuesta abrir(Usuario usuario) {
        Instant ahora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Sesion sesion = sesiones.save(new Sesion(usuario, ahora.plus(VIGENCIA_SESION)));
        return emitir(sesion, ahora);
    }

    @Transactional
    public Optional<LoginRespuesta> renovar(String refreshToken) {
        String huella = huella(refreshToken);
        Sesion sesion = renovaciones.buscarSesionId(huella)
                .flatMap(sesiones::bloquear)
                .orElse(null);
        Instant ahora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        if (sesion == null || !sesion.vigente(ahora)) {
            return Optional.empty();
        }

        TokenRenovacion anterior = renovaciones.findByHuella(huella).orElseThrow();
        if (anterior.isUsado()) {
            sesion.cerrar();
            return Optional.empty();
        }

        anterior.usar();
        return Optional.of(emitir(sesion, ahora));
    }

    @Transactional
    public void cerrar(String refreshToken) {
        renovaciones.buscarSesionId(huella(refreshToken))
                .flatMap(sesiones::bloquear)
                .ifPresent(Sesion::cerrar);
    }

    private LoginRespuesta emitir(Sesion sesion, Instant ahora) {
        Usuario usuario = sesion.getUsuario();
        byte[] secreto = new byte[32];
        aleatorio.nextBytes(secreto);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(secreto);
        renovaciones.save(new TokenRenovacion(huella(refreshToken), sesion));

        long refreshExpiraEn = Duration.between(ahora, sesion.getVenceEn()).toSeconds();
        long expiraEn = Math.min(VIGENCIA_ACCESO.toSeconds(), refreshExpiraEn);
        JwtClaimsSet datosToken = JwtClaimsSet.builder()
                .issuer(SeguridadConfig.EMISOR)
                .audience(List.of(SeguridadConfig.AUDIENCIA))
                .subject(usuario.getId())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(expiraEn))
                .id(UUID.randomUUID().toString())
                .claim("rol", sesion.getRol())
                .claim("sid", sesion.getId())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = emisor.encode(JwtEncoderParameters.from(cabecera, datosToken)).getTokenValue();
        LoginRespuesta.UsuarioSesion cuenta = new LoginRespuesta.UsuarioSesion(
                usuario.getId(), usuario.getNombre(), usuario.getCorreo(), sesion.getRol());

        return new LoginRespuesta(token, "Bearer", expiraEn, cuenta, refreshToken, refreshExpiraEn);
    }

    private String huella(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }
}
