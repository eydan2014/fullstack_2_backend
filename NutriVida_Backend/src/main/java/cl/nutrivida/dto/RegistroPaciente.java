package cl.nutrivida.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record RegistroPaciente(
        @NotBlank(message = "Ingresa el nombre.")
        @Size(min = 3, max = 100, message = "El nombre debe tener de 3 a 100 caracteres.")
        String nombre,

        @NotBlank(message = "Ingresa el correo.")
        @Email(regexp = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+", message = "Ingresa un correo valido.")
        @Size(max = 254, message = "El correo debe tener como maximo 254 caracteres.")
        String correo,

        @NotBlank(message = "Ingresa el RUT.")
        @Size(max = 20, message = "Ingresa un RUT valido.")
        String rut,

        @NotBlank(message = "Ingresa el telefono.")
        @Pattern(regexp = "\\+?[0-9]{9,12}", message = "Ingresa de 9 a 12 digitos, con + opcional.")
        String telefono,

        @NotBlank(message = "Ingresa la clave.")
        @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres.")
        @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        String clave
) {
    public RegistroPaciente {
        nombre = nombre == null ? null : nombre.strip();
        correo = correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
        rut = rut == null ? null : rut.strip();
        telefono = telefono == null ? null : telefono.strip();
    }
}
