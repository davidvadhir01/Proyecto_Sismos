package com.spring.proyectofinal.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DataWarehouseService {
    
    public Map<String, Object> getMapaRiesgoSismico() {
        Map<String, Object> resultado = new HashMap<>();
        
        // Datos simulados por ahora
        List<Map<String, Object>> zonasAltoRiesgo = new ArrayList<>();
        Map<String, Object> zona1 = new HashMap<>();
        zona1.put("estado", "Oaxaca");
        zona1.put("riesgo", 0.85);
        zona1.put("sismos", 156);
        zonasAltoRiesgo.add(zona1);
        
        Map<String, Object> zona2 = new HashMap<>();
        zona2.put("estado", "Guerrero");
        zona2.put("riesgo", 0.78);
        zona2.put("sismos", 203);
        zonasAltoRiesgo.add(zona2);
        
        resultado.put("zonasAltoRiesgo", zonasAltoRiesgo);
        resultado.put("estadisticasPoblacion", new ArrayList<>());
        resultado.put("estadosSuperavitarios", new ArrayList<>());
        
        return resultado;
    }
    
    public Map<String, Object> getAnalisisEconomico() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("rankingProduccion", new ArrayList<>());
        resultado.put("estadosSuperavitarios", new ArrayList<>());
        return resultado;
    }
    
    public Map<String, Object> getTendenciaTemporal() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("años", Arrays.asList(2020, 2021, 2022, 2023, 2024));
        resultado.put("cantidades", Arrays.asList(45, 52, 38, 67, 81));
        resultado.put("impactosPromedios", Arrays.asList(125000.0, 134000.0, 98000.0, 156000.0, 187000.0));
        return resultado;
    }
    
    public Map<String, Object> getAnalisisPoblacional() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalPoblacion", 127000000);
        resultado.put("poblacionEnRiesgo", 44500000);
        resultado.put("estadosAfectados", 8);
        
        List<Map<String, Object>> estadisticas = new ArrayList<>();
        Map<String, Object> est1 = new HashMap<>();
        est1.put("estado", "Oaxaca");
        est1.put("poblacion", 4100000);
        est1.put("riesgo", "alto");
        estadisticas.add(est1);
        
        resultado.put("estadisticasPorEstado", estadisticas);
        return resultado;
    }
    
    public Map<String, Object> getAnalisisMagnitudes() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("magnitudMaxima", 6.2);
        resultado.put("magnitudPromedio", 4.2);
        resultado.put("totalSismos", 15970);
        
        Map<String, Object> distribucion = new HashMap<>();
        distribucion.put("2.0-3.9", 78);
        distribucion.put("4.0-4.9", 18);
        distribucion.put("5.0-5.9", 3.5);
        distribucion.put("6.0+", 0.5);
        
        resultado.put("distribucionMagnitudes", distribucion);
        return resultado;
    }
}