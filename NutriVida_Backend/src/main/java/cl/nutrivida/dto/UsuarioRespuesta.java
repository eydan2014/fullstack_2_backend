package cl.nutrivida.dto;

public record UsuarioRespuesta(String id, String nombre, String correo, String rut,
                               String telefono, String rol, boolean activo) {
}
