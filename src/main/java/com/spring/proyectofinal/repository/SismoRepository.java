package com.spring.proyectofinal.repository;

import com.spring.proyectofinal.model.Sismo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SismoRepository extends JpaRepository<Sismo, Long> {
    
    // Buscar sismos por año
    @Query("SELECT s FROM Sismo s WHERE YEAR(s.fecha) = :año")
    List<Sismo> findByYear(@Param("año") int año);
    
    // Buscar sismos por estado
    List<Sismo> findByEstado(String estado);
    
    // Buscar sismos por magnitud mayor a
    List<Sismo> findByMagnitudGreaterThan(Double magnitud);
    
    // Buscar sismos recientes (últimos 30 días)
    List<Sismo> findByFechaAfter(LocalDateTime fecha);
    
    // Buscar sismos por rango de fechas
    List<Sismo> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Buscar años disponibles
    @Query("SELECT DISTINCT YEAR(s.fecha) FROM Sismo s ORDER BY YEAR(s.fecha) DESC")
    List<Integer> findDistinctYears();
}