package com.spring.proyectofinal.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sismos")
public class Sismo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime fecha;
    private Double magnitud;
    private Double latitud;
    private Double longitud;
    private Double profundidad;
    private String referencia;
    private String estado;
    private String horaUtc;
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public Double getMagnitud() {
        return magnitud;
    }
    
    public void setMagnitud(Double magnitud) {
        this.magnitud = magnitud;
    }
    
    public Double getLatitud() {
        return latitud;
    }
    
    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }
    
    public Double getLongitud() {
        return longitud;
    }
    
    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
    
    public Double getProfundidad() {
        return profundidad;
    }
    
    public void setProfundidad(Double profundidad) {
        this.profundidad = profundidad;
    }
    
    public String getReferencia() {
        return referencia;
    }
    
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getHoraUtc() {
        return horaUtc;
    }
    
    public void setHoraUtc(String horaUtc) {
        this.horaUtc = horaUtc;
    }
    
    // Método personalizado para obtener el año
    public int getYear() {
        return fecha != null ? fecha.getYear() : 0;
    }
}