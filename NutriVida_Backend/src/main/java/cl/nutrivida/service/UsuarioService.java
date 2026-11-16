package cl.nutrivida.service;

import cl.nutrivida.dto.DatosUsuario;
import cl.nutrivida.dto.RegistroPaciente;
import cl.nutrivida.dto.UsuarioRespuesta;
import cl.nutrivida.model.Rol;
import cl.nutrivida.model.Usuario;
import cl.nutrivida.repository.SesionRepository;
import cl.nutrivida.repository.UsuarioRepository;
import jakarta.validation.Validator;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final SesionRepository sesiones;
    private final PasswordEncoder codificador;
    private final Validator validador;

    public UsuarioService(UsuarioRepository usuarios, SesionRepository sesiones,
                          PasswordEncoder codificador, Validator validador) {
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.codificador = codificador;
        this.validador = validador;
    }

    public UsuarioRespuesta buscar(String id) {
        return usuarios.findById(id).map(this::respuesta).orElseThrow(this::noEncontrado);
    }

    public List<UsuarioRespuesta> listar() {
        return usuarios.findAllByOrderByNombreAscCorreoAsc().stream().map(this::respuesta).toList();
    }

    @Transactional
    public UsuarioRespuesta registrar(RegistroPaciente datos) {
        return guardarNuevo(new DatosUsuario(datos.nombre(), datos.correo(), datos.rut(),
                datos.telefono(), datos.clave(), Rol.PACIENTE));
    }

    @Transactional
    public UsuarioRespuesta crear(String actorId, DatosUsuario datos) {
        bloquearCuenta(actorId, actorId);
        if (datos.rol() == Rol.NUTRICIONISTA) {
            throw rolDelDirectorio();
        }
        return guardarNuevo(datos);
    }

    @Transactional
    public UsuarioRespuesta editar(String actorId, String id, DatosUsuario datos) {
        Usuario cuenta = bloquearCuenta(actorId, id);
        if (actorId.equals(id) && datos.rol() != Rol.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes quitarte el rol de administrador.");
        }
        boolean nutricionista = cuenta.getRol().equals(Rol.NUTRICIONISTA.name());
        if (nutricionista != (datos.rol() == Rol.NUTRICIONISTA)) {
            throw rolDelDirectorio();
        }
        Identidad identidad = validarIdentidad(datos);
        comprobarUnicos(datos.correo(), identidad.rut(), id);
        boolean revocar = !cuenta.getRol().equals(datos.rol().name())
                || !cuenta.getCorreo().equals(datos.correo()) || datos.clave() != null;
        cuenta.actualizar(datos.nombre(), datos.correo(), identidad.rut(), identidad.telefono(), datos.rol());
        if (datos.clave() != null) {
            cuenta.cambiarClave(codificarClave(datos.clave()));
        }
        Usuario guardado = guardar(cuenta);
        if (revocar) {
            sesiones.revocarDeUsuario(id);
        }
        return respuesta(guardado);
    }

    @Transactional
    public UsuarioRespuesta cambiarEstado(String actorId, String id, boolean activo) {
        Usuario cuenta = bloquearCuenta(actorId, id);
        if (!activo && actorId.equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes desactivar tu propia cuenta.");
        }
        if (cuenta.isActivo() != activo) {
            if (cuenta.getRol().equals(Rol.NUTRICIONISTA.name())) {
                throw rolDelDirectorio();
            }
            cuenta.cambiarEstado(activo);
            sesiones.revocarDeUsuario(id);
        }
        return respuesta(cuenta);
    }

    @Transactional
    public void prepararAdministrador(String correo, String clave) {
        if ((correo.isBlank() && clave.isEmpty()) || usuarios.existsByRol(Rol.ADMINISTRADOR.name())) {
            return;
        }
        DatosUsuario datos = new DatosUsuario("Administrador inicial", correo, null, null, clave, Rol.ADMINISTRADOR);
        if (!validador.validate(datos).isEmpty() || datos.clave() == null) {
            throw new IllegalStateException("Configura NUTRIVIDA_ADMIN_CORREO y NUTRIVIDA_ADMIN_CLAVE validos.");
        }
        guardarNuevo(datos);
    }

    private UsuarioRespuesta guardarNuevo(DatosUsuario datos) {
        Identidad identidad = validarIdentidad(datos);
        comprobarUnicos(datos.correo(), identidad.rut(), null);
        Usuario cuenta = new Usuario(datos.nombre(), datos.correo(), identidad.rut(), identidad.telefono(),
                codificarClave(datos.clave()), datos.rol());
        return respuesta(guardar(cuenta));
    }

    private Usuario bloquearCuenta(String actorId, String id) {
        List<Usuario> cuentas = usuarios.bloquearPorIds(List.of(actorId, id));
        boolean autorizado = cuentas.stream().anyMatch(cuenta -> cuenta.getId().equals(actorId)
                && cuenta.isActivo() && cuenta.getRol().equals(Rol.ADMINISTRADOR.name()));
        if (!autorizado) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el rol de administrador.");
        }
        return cuentas.stream().filter(cuenta -> cuenta.getId().equals(id)).findFirst()
                .orElseThrow(this::noEncontrado);
    }

    private void comprobarUnicos(String correo, String rut, String id) {
        boolean correoOcupado = usuarios.findByCorreo(correo)
                .filter(cuenta -> !cuenta.getId().equals(id)).isPresent();
        boolean rutOcupado = rut != null && usuarios.findByRut(rut)
                .filter(cuenta -> !cuenta.getId().equals(id)).isPresent();
        if (correoOcupado || rutOcupado) {
            throw duplicado();
        }
    }

    private Usuario guardar(Usuario cuenta) {
        try {
            return usuarios.saveAndFlush(cuenta);
        } catch (DataIntegrityViolationException error) {
            throw duplicado();
        }
    }

    private Identidad validarIdentidad(DatosUsuario datos) {
        if (datos.rol() == Rol.PACIENTE && datos.rut() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa el RUT del paciente.");
        }
        String telefono = datos.telefono();
        if ((datos.rol() == Rol.PACIENTE && telefono == null)
                || (telefono != null && !telefono.matches("\\+?[0-9]{9,12}"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un telefono valido.");
        }
        return new Identidad(datos.rut() == null ? null : normalizarRut(datos.rut()), telefono);
    }

    private String codificarClave(String clave) {
        if (clave == null || clave.isBlank() || clave.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave debe tener al menos 8 caracteres.");
        }
        if (clave.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La clave supera el limite de 72 bytes.");
        }
        return codificador.encode(clave);
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

    private UsuarioRespuesta respuesta(Usuario cuenta) {
        return new UsuarioRespuesta(cuenta.getId(), cuenta.getNombre(), cuenta.getCorreo(), cuenta.getRut(),
                cuenta.getTelefono(), cuenta.getRol(), cuenta.isActivo());
    }

    private ResponseStatusException rutInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un RUT valido con digito verificador.");
    }

    private ResponseStatusException duplicado() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El correo o el RUT ya esta registrado.");
    }

    private ResponseStatusException rolDelDirectorio() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El rol y estado de Nutricionista se gestionan desde el directorio.");
    }

    private ResponseStatusException noEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
    }

    private record Identidad(String rut, String telefono) {
    }
}
