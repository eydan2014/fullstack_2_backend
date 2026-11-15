package cl.nutrivida.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens_renovacion")
public class TokenRenovacion {
    @Id
    @Column(length = 64)
    private String huella;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private Sesion sesion;

    @Column(nullable = false)
    private boolean usado;

    protected TokenRenovacion() {
    }

    public TokenRenovacion(String huella, Sesion sesion) {
        this.huella = huella;
        this.sesion = sesion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void usar() {
        usado = true;
    }
}
