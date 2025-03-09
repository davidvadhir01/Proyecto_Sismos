package com.spring.proyectofinal.repository;

import com.spring.proyectofinal.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    // Buscar administrador por email
    Optional<Administrador> findByEmail(String email);

    // Verificar si existe un administrador con este email
    boolean existsByEmail(String email);

    // Buscar administradores por departamento
    List<Administrador> findByDepartamento(String departamento);

    // Buscar administradores activos
    List<Administrador> findByEstadoActivo(Boolean estadoActivo);
}