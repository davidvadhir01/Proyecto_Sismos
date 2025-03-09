package com.spring.proyectofinal.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Administrador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;

    private String password;
    private String rol; // "ADMIN"

    // Puedes añadir campos adicionales específicos para administradores
    private String departamento;
    private Boolean estadoActivo = true;
}