package com.spring.proyectofinal.service;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.util.SismosCSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

@Service
public class SismoServiceImpl implements SismoService {

    private final SismosCSVReader sismosCSVReader;
    private List<Sismo> sismos;

    @Autowired
    public SismoServiceImpl(SismosCSVReader sismosCSVReader) {
        this.sismosCSVReader = sismosCSVReader;
    }

    @PostConstruct
    public void init() {
        this.sismos = sismosCSVReader.readSismosFromCSV("data/SSNMX_catalogo.csv");
    }

    @Override
    public List<Sismo> getAllSismos() {
        return sismos;
    }

    @Override
    public List<Sismo> getSismosByYear(int year) {
        return sismos.stream()
                .filter(sismo -> sismo.getYear() == year)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Integer> getAvailableYears() {
        Set<Integer> years = new HashSet<>();
        for (Sismo sismo : sismos) {
            years.add(sismo.getYear());
        }
        return years;
    }
}