package cl.nutrivida.controller;

import cl.nutrivida.dto.DatosServicio;
import cl.nutrivida.dto.ServicioEstado;
import cl.nutrivida.model.Servicio;
import cl.nutrivida.service.ServicioService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class ServicioController {
    private final ServicioService servicio;

    public ServicioController(ServicioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/servicios")
    public List<Servicio> listar() {
        return servicio.listar();
    }

    @GetMapping("/servicios/{id}")
    @ApiResponse(responseCode = "200", description = "Servicio encontrado")
    @ApiResponse(responseCode = "404", description = "Servicio no encontrado", content = @Content)
    public ResponseEntity<Servicio> buscar(@PathVariable("id") String id) {
        return ResponseEntity.of(servicio.buscar(id));
    }

    @GetMapping("/admin/servicios")
    @SecurityRequirement(name = "jwt")
    public List<Servicio> listarAdministracion() {
        return servicio.listarAdministracion();
    }

    @PostMapping("/servicios")
    @SecurityRequirement(name = "jwt")
    @ApiResponse(responseCode = "201", description = "Servicio creado")
    @ApiResponse(responseCode = "409", description = "Codigo duplicado", content = @Content)
    public ResponseEntity<Servicio> crear(@Valid @RequestBody DatosServicio datos) {
        Servicio nuevo = servicio.crear(datos);
        return ResponseEntity.created(URI.create("/api/v1/servicios/" + nuevo.getId())).body(nuevo);
    }

    @PutMapping("/servicios/{id}")
    @SecurityRequirement(name = "jwt")
    public Servicio editar(@PathVariable("id") String id, @Valid @RequestBody DatosServicio datos) {
        return servicio.editar(id, datos);
    }

    @PatchMapping("/servicios/{id}/estado")
    @SecurityRequirement(name = "jwt")
    @ApiResponse(responseCode = "409", description = "El servicio tiene citas activas", content = @Content)
    @ApiResponse(responseCode = "503", description = "No se pudo comprobar las citas del servicio", content = @Content)
    public Servicio estado(@PathVariable("id") String id, @Valid @RequestBody ServicioEstado estado) {
        return servicio.cambiarEstado(id, estado.activo());
    }

    @DeleteMapping("/servicios/{id}")
    @SecurityRequirement(name = "jwt")
    @ApiResponse(responseCode = "204", description = "Servicio desactivado", content = @Content)
    @ApiResponse(responseCode = "409", description = "El servicio tiene citas activas", content = @Content)
    @ApiResponse(responseCode = "503", description = "No se pudo comprobar las citas del servicio", content = @Content)
    public ResponseEntity<Void> desactivar(@PathVariable("id") String id) {
        servicio.cambiarEstado(id, false);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail errorDeServicio(ResponseStatusException error) {
        return error.getBody();
    }
}
