package cl.nutrivida.controller;

import cl.nutrivida.dto.PacienteRegistrado;
import cl.nutrivida.dto.RegistroPaciente;
import cl.nutrivida.service.RegistroPacienteService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistroPacienteController {
    private final RegistroPacienteService servicio;

    public RegistroPacienteController(RegistroPacienteService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Paciente registrado")
    @ApiResponse(responseCode = "400", description = "Datos invalidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Correo o RUT ya registrado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public PacienteRegistrado registrar(@Valid @RequestBody RegistroPaciente datos) {
        return servicio.registrar(datos);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail datosInvalidos(MethodArgumentNotValidException error) {
        Map<String, String> errores = new LinkedHashMap<>();
        error.getBindingResult().getFieldErrors().forEach(campo ->
                errores.putIfAbsent(campo.getField(), campo.getDefaultMessage()));
        ProblemDetail detalle = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Revisa los datos del registro.");
        detalle.setProperty("errores", errores);
        return detalle;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail errorDeRegistro(ResponseStatusException error) {
        return error.getBody();
    }
}
