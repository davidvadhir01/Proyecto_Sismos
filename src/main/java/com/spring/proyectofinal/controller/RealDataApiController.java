package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.service.RealDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datos-reales")
@CrossOrigin(origins = "*")
public class RealDataApiController {

    @Autowired
    private RealDataService realDataService;

    /**
     * API para obtener datos sísmicos reales del DataWarehouse
     * Utilizada por el mapa de magnitudes
     */
    @GetMapping("/sismicos")
    public ResponseEntity<Map<String, Object>> getDatosSismicos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rangoMagnitud,
            @RequestParam(required = false) String periodo) {
        
        try {
            Map<String, Object> datos = realDataService.getDatosSismicos();
            
            // Si hay filtros específicos, aplicarlos aquí
            if (estado != null && !estado.isEmpty()) {
                Map<String, Object> datosFiltrados = Map.of(estado, datos.get(estado));
                return ResponseEntity.ok(datosFiltrados);
            }
            
            return ResponseEntity.ok(datos);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo datos sísmicos: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error obteniendo datos sísmicos: " + e.getMessage()));
        }
    }

    /**
     * API para obtener datos económicos reales del DataWarehouse
     * Utilizada por el mapa económico
     */
    @GetMapping("/economicos")
    public ResponseEntity<Map<String, Object>> getDatosEconomicos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double minPIB,
            @RequestParam(required = false) String vulnerabilidad) {
        
        try {
            Map<String, Object> datos = realDataService.getDatosEconomicos();
            return ResponseEntity.ok(datos);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo datos económicos: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error obteniendo datos económicos: " + e.getMessage()));
        }
    }

    /**
     * API para obtener datos poblacionales reales del DataWarehouse
     * Utilizada por el mapa poblacional
     */
    @GetMapping("/poblacionales")
    public ResponseEntity<Map<String, Object>> getDatosPoblacionales(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double minPoblacion,
            @RequestParam(required = false) String nivelRiesgo) {
        
        try {
            Map<String, Object> datos = realDataService.getDatosPoblacionales();
            return ResponseEntity.ok(datos);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo datos poblacionales: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error obteniendo datos poblacionales: " + e.getMessage()));
        }
    }

    /**
     * API para obtener eventos sísmicos recientes
     * Utilizada por el monitoreo en tiempo real
     */
    @GetMapping("/eventos-recientes")
    public ResponseEntity<List<Map<String, Object>>> getEventosRecientes(
            @RequestParam(required = false, defaultValue = "10") Integer limite) {
        
        try {
            List<Map<String, Object>> eventos = realDataService.getEventosRecientes(limite);
            return ResponseEntity.ok(eventos);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo eventos recientes: " + e.getMessage());
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * API para obtener estadísticas generales del dashboard
     */
    @GetMapping("/estadisticas-dashboard")
    public ResponseEntity<Map<String, Object>> getEstadisticasDashboard() {
        try {
            Map<String, Object> stats = realDataService.getEstadisticasDashboard();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo estadísticas dashboard: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error obteniendo estadísticas: " + e.getMessage()));
        }
    }

    /**
     * API para verificar el estado del DataWarehouse
     */
    @GetMapping("/estado-datawarehouse")
    public ResponseEntity<Map<String, Object>> getEstadoDataWarehouse() {
        try {
            Map<String, Object> estado = realDataService.getEstadisticasDashboard();
            
            Map<String, Object> resumen = Map.of(
                "disponible", estado.get("databasePopulated"),
                "totalSismos", estado.get("totalSismos"),
                "totalZonas", estado.get("totalZonas"),
                "totalEconomia", estado.get("totalEconomia"),
                "totalHechos", estado.get("totalHechos"),
                "ultimaActualizacion", new java.util.Date()
            );
            
            return ResponseEntity.ok(resumen);
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "disponible", false,
                "error", "DataWarehouse no disponible",
                "mensaje", "Usando datos simulados"
            ));
        }
    }

    /**
     * API para obtener lista de estados disponibles
     */
    @GetMapping("/estados-disponibles")
    public ResponseEntity<List<String>> getEstadosDisponibles() {
        try {
            Map<String, Object> datos = realDataService.getDatosSismicos();
            List<String> estados = new java.util.ArrayList<>(datos.keySet());
            estados.sort(String::compareTo);
            
            return ResponseEntity.ok(estados);
            
        } catch (Exception e) {
            // Lista de estados mexicanos por defecto
            List<String> estadosDefault = List.of(
                "Aguascalientes", "Baja California", "Baja California Sur", "Campeche", "Coahuila",
                "Colima", "Chiapas", "Chihuahua", "Ciudad de Mexico", "Durango", "Guanajuato",
                "Guerrero", "Hidalgo", "Jalisco", "Mexico", "Michoacan", "Morelos", "Nayarit",
                "Nuevo Leon", "Oaxaca", "Puebla", "Queretaro", "Quintana Roo", "San Luis Potosi",
                "Sinaloa", "Sonora", "Tabasco", "Tamaulipas", "Tlaxcala", "Veracruz", "Yucatan", "Zacatecas"
            );
            
            return ResponseEntity.ok(estadosDefault);
        }
    }

    /**
     * API para obtener resumen de un estado específico
     */
    @GetMapping("/resumen-estado/{estado}")
    public ResponseEntity<Map<String, Object>> getResumenEstado(@PathVariable String estado) {
        try {
            Map<String, Object> datosSismicos = realDataService.getDatosSismicos();
            Map<String, Object> datosEconomicos = realDataService.getDatosEconomicos();
            Map<String, Object> datosPoblacionales = realDataService.getDatosPoblacionales();
            
            Map<String, Object> resumen = new java.util.HashMap<>();
            
            // Datos sísmicos del estado
            if (datosSismicos.containsKey(estado)) {
                resumen.put("sismicos", datosSismicos.get(estado));
            }
            
            // Datos económicos del estado
            if (datosEconomicos.containsKey(estado)) {
                resumen.put("economicos", datosEconomicos.get(estado));
            }
            
            // Datos poblacionales del estado
            if (datosPoblacionales.containsKey(estado)) {
                resumen.put("poblacionales", datosPoblacionales.get(estado));
            }
            
            if (resumen.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            resumen.put("estado", estado);
            resumen.put("fechaConsulta", new java.util.Date());
            
            return ResponseEntity.ok(resumen);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error obteniendo resumen del estado: " + e.getMessage()));
        }
    }
}