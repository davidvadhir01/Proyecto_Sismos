// FactImpactoSismos.java
package com.spring.proyectofinal.model.datawarehouse;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "fact_impacto_sismos_imputed")
public class FactImpactoSismos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSismo;
    
    @ManyToOne
    @JoinColumn(name = "ID_zonas")
    private DimZona zona;
    
    @ManyToOne
    @JoinColumn(name = "ID_economia")
    private DimEconomia economia;
    
    @ManyToOne
    @JoinColumn(name = "ID_tiempo")
    private DimTiempo tiempo;
    
    private Long poblacionAfectada;
    private Double impactoEconomico;
    private Double determinanteRiesgo;
    private Double riesgoProporcional;
    private Double indiceZscore;
}