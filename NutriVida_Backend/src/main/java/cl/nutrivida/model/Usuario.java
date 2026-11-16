package cl.nutrivida.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(unique = true, length = 10)
    private String rut;

    @Column(length = 13)
    private String telefono;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    protected Usuario() {
    }

    public Usuario(String nombre, String correo, String rut, String telefono, String claveHash, Rol rol) {
        actualizar(nombre, correo, rut, telefono, rol);
        this.claveHash = claveHash;
    }

    public void actualizar(String nombre, String correo, String rut, String telefono, Rol rol) {
        this.nombre = nombre;
        this.correo = correo;
        this.rut = rut;
        this.telefono = telefono;
        this.rol = rol.name();
    }

    public void cambiarClave(String claveHash) {
        this.claveHash = claveHash;
    }

    public void cambiarEstado(boolean activo) {
        this.activo = activo;
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

    public String getRut() {
        return rut;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getRol() {
        return rol;
    }

    @JsonIgnore
    public String getClaveHash() {
        return claveHash;
    }

    public boolean isActivo() {
        return activo;
    }
}
