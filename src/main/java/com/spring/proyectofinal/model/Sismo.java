// Modelo Sismo.java
package com.spring.proyectofinal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sismo {
    private LocalDateTime fecha;
    private double magnitud;
    private double latitud;
    private double longitud;
    private double profundidad;
    private String referencia;
    private String estado;
    private int year;

    // Método para establecer el año a partir de la fecha
    public void setYearFromDate() {
        if (fecha != null) {
            this.year = fecha.getYear();
        }
    }
}
