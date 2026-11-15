package cl.nutrivida.dto;

public record LoginRespuesta(String token, String tipo, long expiraEn, UsuarioSesion usuario,
                             String refreshToken, long refreshExpiraEn) {
    public record UsuarioSesion(String id, String nombre, String correo, String rol) {
    }
}
