package cl.nutrivida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RenovacionSolicitud(
        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9_-]{43}")
        @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
        String refreshToken
) {
}
