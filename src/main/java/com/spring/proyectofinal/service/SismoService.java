package com.spring.proyectofinal.service;

import com.spring.proyectofinal.model.Sismo;
import java.util.List;
import java.util.Set;

public interface SismoService {
    List<Sismo> getAllSismos();
    List<Sismo> getSismosByYear(int year);
    Set<Integer> getAvailableYears();
}
