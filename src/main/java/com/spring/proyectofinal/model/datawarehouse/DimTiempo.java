// DimTiempo.java
package com.spring.proyectofinal.model.datawarehouse;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "dim_tiempo")
public class DimTiempo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTiempo;
    
    private LocalDateTime fecha;
    private String horaUtc;
    private Integer anio;
    private Integer mes;
    private Integer dia;
    private Integer trimestre;
}