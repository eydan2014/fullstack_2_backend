package cl.nutrivida.controller;

import cl.nutrivida.dto.LoginRespuesta;
import cl.nutrivida.dto.LoginSolicitud;
import cl.nutrivida.service.InicioSesionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@SecurityScheme(name = "jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class InicioSesionController {
    private final InicioSesionService servicio;

    public InicioSesionController(InicioSesionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/login")
    @ApiResponse(responseCode = "200", description = "Sesion iniciada")
    @ApiResponse(responseCode = "401", description = "Credenciales invalidas", content = @Content)
    public ResponseEntity<LoginRespuesta> iniciar(@Valid @RequestBody LoginSolicitud datos) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(servicio.iniciar(datos));
    }

    @GetMapping("/sesion")
    @SecurityRequirement(name = "jwt")
    @ApiResponse(responseCode = "401", description = "Falta un token valido", content = @Content)
    @ApiResponse(responseCode = "403", description = "La cuenta no tiene el rol Paciente", content = @Content)
    public SesionActiva sesion(JwtAuthenticationToken token) {
        return new SesionActiva(token.getName(), token.getToken().getClaimAsString("rol"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail errorDeSesion(ResponseStatusException error) {
        return error.getBody();
    }

    public record SesionActiva(String id, String rol) {
    }
}
