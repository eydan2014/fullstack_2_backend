package cl.nutrivida.dto;

import jakarta.validation.constraints.NotNull;

public record ServicioEstado(@NotNull Boolean activo) {
}
