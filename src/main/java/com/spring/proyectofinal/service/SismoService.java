package com.spring.proyectofinal.service;

import com.spring.proyectofinal.model.Sismo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SismoService {

    List<Sismo> obtenerTodosLosSismos();

    Optional<Sismo> obtenerSismoPorId(Long id);

    void eliminarSismo(Long id);

    Sismo save(Sismo sismo);

    List<Sismo> saveAll(List<Sismo> sismos);

    List<Sismo> obtenerSismosPorAño(int año);

    List<Integer> getAvailableYears();

    List<Sismo> getSismosByYear(Integer year);

    List<Sismo> getAllSismos();

    List<Sismo> obtenerSismosRecientes();
} 
