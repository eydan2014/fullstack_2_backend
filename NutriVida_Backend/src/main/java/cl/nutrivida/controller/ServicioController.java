package cl.nutrivida.controller;

import cl.nutrivida.model.Servicio;
import cl.nutrivida.service.ServicioService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/servicios")
public class ServicioController {
    private final ServicioService servicio;

    public ServicioController(ServicioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<Servicio> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Servicio encontrado")
    @ApiResponse(responseCode = "404", description = "Servicio no encontrado", content = @Content)
    public ResponseEntity<Servicio> buscar(@PathVariable("id") String id) {
        return ResponseEntity.of(servicio.buscar(id));
    }
}
