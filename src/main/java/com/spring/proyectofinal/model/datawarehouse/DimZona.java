package com.spring.proyectofinal.model.datawarehouse;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "dim_zonas")
public class DimZona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idZonas;
    
    private String entidad;
    private String nomEnt;
    private Long pobtot;
    private Long pobfem;
    private Long pobmas;
}