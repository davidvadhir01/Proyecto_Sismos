package com.spring.proyectofinal.model.datawarehouse;

import jakarta.persistence.*;
import lombok.Data;

@Data  // ← ESTA ES LA CLAVE
@Entity
@Table(name = "estadisticas_sismos")
public class EstadisticaSismo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String estado;
    private Long totalSismos;
    private Double indiceRiesgo;
    private Long poblacionAfectada;
    private Double impactoEconomico;
    
    // @Data genera automáticamente todos los getters y setters
}