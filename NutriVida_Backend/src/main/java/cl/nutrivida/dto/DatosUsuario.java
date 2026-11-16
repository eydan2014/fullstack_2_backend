package cl.nutrivida.dto;

import cl.nutrivida.model.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record DatosUsuario(
        @NotBlank @Size(min = 3, max = 100) String nombre,
        @NotBlank @Email(regexp = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+") @Size(max = 254) String correo,
        @Size(max = 20) String rut,
        @Size(max = 13) String telefono,
        @Size(min = 8, max = 72)
        @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        String clave,
        @NotNull Rol rol
) {
    public DatosUsuario {
        nombre = nombre == null ? null : nombre.strip();
        correo = correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
        rut = rut == null || rut.isBlank() ? null : rut.strip();
        telefono = telefono == null || telefono.isBlank() ? null : telefono.strip();
        clave = clave == null || clave.isEmpty() ? null : clave;
    }
}
