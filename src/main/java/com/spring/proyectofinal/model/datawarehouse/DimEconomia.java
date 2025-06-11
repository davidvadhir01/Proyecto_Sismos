// DimEconomia.java
package com.spring.proyectofinal.model.datawarehouse;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dim_economia")
public class DimEconomia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEconomia;
    
    private String nombreEntidad;
    private Double produccionBrutaTotal;
    private Double insumosUtilizados;
    private Double consumoIntermedio;
    private Double valorAgregado;
    private Double formacionCapital;
    private Double activosFijosAdquiridos;
}