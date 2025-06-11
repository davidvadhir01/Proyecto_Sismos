package com.spring.proyectofinal.service;

import com.spring.proyectofinal.model.Sismo;
import java.util.List;
import java.util.Optional;

public interface SismoService {
    
    // Métodos básicos CRUD
    List<Sismo> obtenerTodosLosSismos();
    Optional<Sismo> obtenerSismoPorId(Long id);
    void eliminarSismo(Long id);
    Sismo save(Sismo sismo);
    List<Sismo> saveAll(List<Sismo> sismos);
    
    // Métodos por año
    List<Sismo> obtenerSismosPorAño(int año);
    List<Integer> getAvailableYears();
    
    // Métodos que usa el controller
    List<Sismo> getSismosByYear(Integer year);
    List<Sismo> getAllSismos();
    
    // Método para sismos recientes
    List<Sismo> obtenerSismosRecientes();
}