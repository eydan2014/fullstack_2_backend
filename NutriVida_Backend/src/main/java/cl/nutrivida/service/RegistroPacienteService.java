package cl.nutrivida.service;

import cl.nutrivida.dto.PacienteRegistrado;
import cl.nutrivida.dto.RegistroPaciente;
import cl.nutrivida.model.Usuario;
import cl.nutrivida.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistroPacienteService {
    private final UsuarioRepository usuarios;
    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder();

    public RegistroPacienteService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public PacienteRegistrado registrar(RegistroPaciente datos) {
        String rut = normalizarRut(datos.rut());
        if (datos.clave().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave supera el limite de 72 bytes.");
        }
        if (usuarios.existsByCorreoOrRut(datos.correo(), rut)) {
            throw registroDuplicado();
        }

        Usuario usuario = new Usuario(datos.nombre(), datos.correo(), rut,
                datos.telefono(), codificador.encode(datos.clave()));
        try {
            Usuario guardado = usuarios.saveAndFlush(usuario);
            return new PacienteRegistrado(guardado.getId(), guardado.getNombre(),
                    guardado.getCorreo(), guardado.getRol());
        } catch (DataIntegrityViolationException error) {
            throw registroDuplicado();
        }
    }

    private String normalizarRut(String rut) {
        String limpio = rut.replaceAll("[.\\s-]", "").toUpperCase(Locale.ROOT);
        if (!limpio.matches("[0-9]{7,8}[0-9K]")) {
            throw rutInvalido();
        }
        String numero = limpio.substring(0, limpio.length() - 1).replaceFirst("^0+", "");
        if (numero.length() < 7) {
            throw rutInvalido();
        }
        int suma = 0;
        int factor = 2;
        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += (numero.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resto = 11 - suma % 11;
        char digito = resto == 11 ? '0' : resto == 10 ? 'K' : (char) ('0' + resto);
        if (digito != limpio.charAt(limpio.length() - 1)) {
            throw rutInvalido();
        }
        return numero + "-" + digito;
    }

    private ResponseStatusException rutInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un RUT valido con digito verificador.");
    }

    private ResponseStatusException registroDuplicado() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El correo o el RUT ya esta registrado.");
    }
}
