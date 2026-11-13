package cl.nutrivida.controller;

import cl.nutrivida.model.Servicio;
import cl.nutrivida.service.ServicioService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
}
