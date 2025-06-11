package com.spring.proyectofinal.service;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.repository.SismoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;  // ← ESTE IMPORT FALTABA
import java.util.Set;
import java.util.HashSet;

@Service
public class SismoServiceImpl implements SismoService {

    @Autowired
    private SismoRepository sismoRepository;

    @Override
    public List<Sismo> obtenerTodosLosSismos() {
        return sismoRepository.findAll();
    }

    @Override
    public Optional<Sismo> obtenerSismoPorId(Long id) {
        return sismoRepository.findById(id);
    }

    @Override
    public void eliminarSismo(Long id) {
        sismoRepository.deleteById(id);
    }

    @Override
    public Sismo save(Sismo sismo) {
        return sismoRepository.save(sismo);
    }

    @Override
    public List<Sismo> saveAll(List<Sismo> sismos) {
        return sismoRepository.saveAll(sismos);
    }

    @Override
    public List<Sismo> obtenerSismosPorAño(int año) {
        // Usa el método optimizado del repository
        return sismoRepository.findByYear(año);
    }

    @Override
    public List<Integer> getAvailableYears() {
        // Usa el método optimizado del repository
        return sismoRepository.findDistinctYears();
    }

    @Override
    public List<Sismo> getSismosByYear(Integer year) {
        if (year == null) {
            return getAllSismos();
        }
        return obtenerSismosPorAño(year);
    }

    @Override
    public List<Sismo> getAllSismos() {
        return obtenerTodosLosSismos();
    }

    @Override
    public List<Sismo> obtenerSismosRecientes() {
        // Usa el método optimizado del repository
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        return sismoRepository.findByFechaAfter(hace30Dias);
    }
}