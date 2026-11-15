package cl.nutrivida.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sesiones")
public class Sesion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;

    @Column(nullable = false)
    private boolean activa = true;

    protected Sesion() {
    }

    public Sesion(Usuario usuario, Instant venceEn) {
        this.usuario = usuario;
        this.rol = usuario.getRol();
        this.venceEn = venceEn;
    }

    public String getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    public Instant getVenceEn() {
        return venceEn;
    }

    public boolean vigente(Instant ahora) {
        return activa && venceEn.isAfter(ahora) && usuario.isActivo() && rol.equals(usuario.getRol());
    }

    public void cerrar() {
        activa = false;
    }
}
