package cl.nutrivida.controller;

import cl.nutrivida.dto.ReporteAtenciones;
import cl.nutrivida.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/reportes")
@SecurityRequirement(name = "jwt")
public class ReporteController {
    private final ReporteService reportes;

    public ReporteController(ReporteService reportes) {
        this.reportes = reportes;
    }

    @GetMapping("/atenciones")
    @Operation(summary = "Consulta el reporte de atenciones",
            description = "Las fechas son inclusivas y usan AAAA-MM-DD. Sin fechas se consulta el mes actual hasta hoy.")
    @ApiResponse(responseCode = "200", description = "Reporte de atenciones")
    @ApiResponse(responseCode = "400", description = "Fechas invalidas", content = @Content)
    @ApiResponse(responseCode = "401", description = "Sesion requerida", content = @Content)
    @ApiResponse(responseCode = "403", description = "Solo para administradores", content = @Content)
    @ApiResponse(responseCode = "503", description = "No se pudieron consultar las citas", content = @Content)
    public ReporteAtenciones atenciones(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "profesionalId", required = false) String profesionalId) {
        return reportes.atenciones(desde, hasta, profesionalId);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail errorDeReporte(ResponseStatusException error) {
        return error.getBody();
    }
}
