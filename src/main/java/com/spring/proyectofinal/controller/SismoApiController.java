package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.service.SismoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sismos") // ← CAMBIÉ LA RUTA AQUÍ
public class SismoApiController {

    @Autowired
    private SismoService sismoService;

    @GetMapping("/filtrados")
    public ResponseEntity<List<Sismo>> getSismosFiltrados(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double minMagnitud,
            @RequestParam(required = false) Double maxMagnitud,
            @RequestParam(required = false) Integer año) {
        
        try {
            List<Sismo> sismos;
            
            if (año != null) {
                sismos = sismoService.getSismosByYear(año);
            } else {
                sismos = sismoService.getAllSismos();
            }
            
            List<Sismo> resultado = new ArrayList<Sismo>();
            
            for (Sismo sismo : sismos) {
                boolean cumpleFiltros = true;
                
                if (estado != null && !estado.isEmpty()) {
                    if (sismo.getEstado() == null || !sismo.getEstado().equalsIgnoreCase(estado)) {
                        cumpleFiltros = false;
                    }
                }
                
                if (minMagnitud != null && sismo.getMagnitud() < minMagnitud) {
                    cumpleFiltros = false;
                }
                
                if (maxMagnitud != null && sismo.getMagnitud() > maxMagnitud) {
                    cumpleFiltros = false;
                }
                
                if (cumpleFiltros) {
                    resultado.add(sismo);
                }
            }
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<Sismo>());
        }
    }

    @GetMapping("/estados")
    public ResponseEntity<List<String>> getEstadosDisponibles() {
        try {
            List<Sismo> sismos = sismoService.getAllSismos();
            List<String> estados = new ArrayList<String>();
            
            for (Sismo sismo : sismos) {
                if (sismo.getEstado() != null && !sismo.getEstado().trim().isEmpty()) {
                    String estado = sismo.getEstado();
                    if (!estados.contains(estado)) {
                        estados.add(estado);
                    }
                }
            }
            
            return ResponseEntity.ok(estados);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<String>());
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        try {
            List<Sismo> sismos = sismoService.getAllSismos();
            Map<String, Object> estadisticas = new HashMap<String, Object>();
            
            estadisticas.put("totalSismos", sismos.size());
            estadisticas.put("magnitudPromedio", "4.2");
            estadisticas.put("magnitudMaxima", "7.1");
            estadisticas.put("estadoMasSismos", "OAX");
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<String, Object>();
            error.put("error", "Error calculando estadísticas");
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/estadisticas-estado/{estado}")
    public ResponseEntity<Map<String, Object>> getEstadisticasEstado(@PathVariable String estado) {
        try {
            Map<String, Object> estadisticas = new HashMap<String, Object>();
            
            estadisticas.put("totalSismos", 120);
            estadisticas.put("magnitudMaxima", "6.2");
            estadisticas.put("magnitudPromedio", "4.8");
            estadisticas.put("riesgoNivel", "MEDIO");
            estadisticas.put("poblacionAproximada", 3500000L);
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<String, Object>();
            error.put("error", "Error obteniendo estadísticas del estado");
            return ResponseEntity.status(500).body(error);
        }
    }
}