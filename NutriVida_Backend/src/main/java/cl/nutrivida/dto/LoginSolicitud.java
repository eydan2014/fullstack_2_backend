package cl.nutrivida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record LoginSolicitud(
        @NotBlank @Email @Size(max = 254)
        String correo,

        @NotBlank @Size(max = 72)
        @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        String clave
) {
    public LoginSolicitud {
        correo = correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
    }
}
