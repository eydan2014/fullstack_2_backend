package cl.nutrivida.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "servicios")
public class Servicio {
    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String modalidad;
    private BigDecimal precio;

    @Column(name = "duracion_minutos")
    private int duracionMinutos;

    private boolean activo;

    protected Servicio() {
    }

    public Servicio(String id, String nombre, String descripcion, String tipo, String modalidad,
                    BigDecimal precio, int duracionMinutos) {
        this.id = id;
        this.activo = true;
        actualizar(nombre, descripcion, tipo, modalidad, precio, duracionMinutos);
    }

    public void actualizar(String nombre, String descripcion, String tipo, String modalidad,
                           BigDecimal precio, int duracionMinutos) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.modalidad = modalidad;
        this.precio = precio;
        this.duracionMinutos = duracionMinutos;
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

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getModalidad() {
        return modalidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public boolean isActivo() {
        return activo;
    }
}
