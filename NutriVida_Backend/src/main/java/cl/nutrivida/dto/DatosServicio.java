package cl.nutrivida.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Locale;

public record DatosServicio(
        @Pattern(regexp = "[A-Z]{2}[0-9]{3}") String id,
        @NotBlank @Size(min = 4, max = 255) String nombre,
        @NotBlank @Size(max = 255) String descripcion,
        @NotBlank @Size(max = 255) String tipo,
        @NotBlank @Size(max = 255) String modalidad,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @NotNull @Positive @Max(1440) Integer duracionMinutos
) {
    public DatosServicio {
        id = id == null ? null : id.strip().toUpperCase(Locale.ROOT);
        nombre = limpiar(nombre);
        descripcion = limpiar(descripcion);
        tipo = limpiar(tipo);
        modalidad = limpiar(modalidad);
    }

    private static String limpiar(String valor) {
        return valor == null ? null : valor.strip();
    }
}
