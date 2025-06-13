package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.service.SismoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class SismoController {

    private final SismoService sismoService;

    @Autowired
    public SismoController(SismoService sismoService) {
        this.sismoService = sismoService;
    }

    @GetMapping("/mapa")
    public String mapa(@RequestParam(required = false) Integer year, Model model) {
        try {
            List<Integer> años = sismoService.getAvailableYears();
            List<Sismo> sismos;
            
            if (year != null) {
                sismos = sismoService.getSismosByYear(year);
            } else {
                sismos = sismoService.getAllSismos();
            }
            
            model.addAttribute("years", años);
            model.addAttribute("selectedYear", year);
            model.addAttribute("totalSismos", sismos.size());
            
            // Estadísticas adicionales
            if (!sismos.isEmpty()) {
                double magnitudPromedio = sismos.stream()
                    .mapToDouble(Sismo::getMagnitud)
                    .average()
                    .orElse(0.0);
                
                double magnitudMaxima = sismos.stream()
                    .mapToDouble(Sismo::getMagnitud)
                    .max()
                    .orElse(0.0);
                
                model.addAttribute("magnitudPromedio", Math.round(magnitudPromedio * 100.0) / 100.0);
                model.addAttribute("magnitudMaxima", magnitudMaxima);
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando datos de sismos: " + e.getMessage());
            model.addAttribute("years", List.of());
            model.addAttribute("totalSismos", 0);
        }
        
        return "mapa";
    }

    // API REST para obtener sismos (usado por JavaScript) - CAMBIÉ LA RUTA
    @GetMapping("/api/sismos-data")
    @ResponseBody
    public ResponseEntity<List<Sismo>> getSismos(@RequestParam(required = false) Integer year) {
        try {
            List<Sismo> sismos;
            
            if (year != null) {
                sismos = sismoService.getSismosByYear(year);
            } else {
                sismos = sismoService.getAllSismos();
            }
            
            return ResponseEntity.ok(sismos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    // COMENTÉ EL MÉTODO CONFLICTIVO - El SismoApiController maneja las estadísticas
    /*
    @GetMapping("/api/sismos/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEstadisticasSismos(@RequestParam(required = false) Integer year) {
        // ... código comentado para evitar conflicto
    }
    */
    
    // API para años disponibles - CAMBIÉ LA RUTA
    @GetMapping("/api/sismos-years")
    @ResponseBody
    public ResponseEntity<List<Integer>> getAñosDisponibles() {
        try {
            List<Integer> años = sismoService.getAvailableYears();
            return ResponseEntity.ok(años);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
}