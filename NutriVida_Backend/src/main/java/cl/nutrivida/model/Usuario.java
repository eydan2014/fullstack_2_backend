package cl.nutrivida.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 254)
    private String correo;

    @Column(nullable = false, unique = true, length = 10)
    private String rut;

    @Column(nullable = false, length = 13)
    private String telefono;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Column(nullable = false, length = 20)
    private String rol = "PACIENTE";

    @Column(nullable = false)
    private boolean activo = true;

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String rut, String telefono, String claveHash) {
        this.nombre = nombre;
        this.correo = correo;
        this.rut = rut;
        this.telefono = telefono;
        this.claveHash = claveHash;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() {
        return rol;
    }
}
