package cl.nutrivida.controller;

import cl.nutrivida.dto.DatosUsuario;
import cl.nutrivida.dto.UsuarioEstado;
import cl.nutrivida.dto.UsuarioRespuesta;
import cl.nutrivida.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = "jwt")
public class UsuarioController {
    private final UsuarioService usuarios;

    public UsuarioController(UsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    @GetMapping("/me")
    public UsuarioRespuesta perfil(JwtAuthenticationToken actor) {
        return usuarios.buscar(actor.getName());
    }

    @GetMapping
    public List<UsuarioRespuesta> listar() {
        return usuarios.listar();
    }

    @GetMapping("/{id}")
    public UsuarioRespuesta buscar(@PathVariable String id) {
        return usuarios.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuesta crear(JwtAuthenticationToken actor, @Valid @RequestBody DatosUsuario datos) {
        return usuarios.crear(actor.getName(), datos);
    }

    @PutMapping("/{id}")
    public UsuarioRespuesta editar(JwtAuthenticationToken actor, @PathVariable String id,
                                    @Valid @RequestBody DatosUsuario datos) {
        return usuarios.editar(actor.getName(), id, datos);
    }

    @PatchMapping("/{id}/estado")
    public UsuarioRespuesta estado(JwtAuthenticationToken actor, @PathVariable String id,
                                    @Valid @RequestBody UsuarioEstado estado) {
        return usuarios.cambiarEstado(actor.getName(), id, estado.activo());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail errorDeUsuario(ResponseStatusException error) {
        return error.getBody();
    }
}
