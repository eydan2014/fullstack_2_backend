package cl.nutrivida.service;

import cl.nutrivida.dto.LoginRespuesta;
import cl.nutrivida.dto.LoginSolicitud;
import cl.nutrivida.model.Usuario;
import cl.nutrivida.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InicioSesionService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final SesionService sesiones;
    private final String hashSinCuenta;

    public InicioSesionService(UsuarioRepository usuarios, PasswordEncoder codificador, SesionService sesiones) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.sesiones = sesiones;
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

        return sesiones.abrir(usuario);
    }
}
