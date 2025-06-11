package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.service.DataWarehouseService;
import com.spring.proyectofinal.service.SismoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/mapas")
public class MapasController {

    @Autowired
    private DataWarehouseService dataWarehouseService;
    
    @Autowired
    private SismoService sismoService;

    // === PÁGINA PRINCIPAL DE MAPAS ===
    @GetMapping
    public String mapasMenu(Model model) {
        try {
            // Verificar disponibilidad de datos del DataWarehouse
            Map<String, Object> datosRiesgo = dataWarehouseService.getMapaRiesgoSismico();
            Map<String, Object> datosEconomicos = dataWarehouseService.getAnalisisEconomico();
            
            // Obtener estadísticas básicas
            Integer totalSismos = sismoService.obtenerTodosLosSismos().size();
            List<Integer> añosDisponibles = sismoService.getAvailableYears();
            
            // Enviar datos a la vista
            model.addAttribute("datosDisponibles", true);
            model.addAttribute("totalSismos", totalSismos);
            model.addAttribute("añosDisponibles", añosDisponibles);
            model.addAttribute("datosRiesgo", datosRiesgo);
            model.addAttribute("datosEconomicos", datosEconomicos);
            
        } catch (Exception e) {
            model.addAttribute("datosDisponibles", false);
            model.addAttribute("error", "Algunos datos de análisis están en preparación: " + e.getMessage());
        }
        
        return "mapas/menu";
    }

    // === MAPAS INDIVIDUALES ===

    @GetMapping("/poblacion")
    public String mapaPoblacion(Model model) {
        try {
            Map<String, Object> datosPoblacion = dataWarehouseService.getAnalisisPoblacional();
            model.addAttribute("datosPoblacion", datosPoblacion);
            model.addAttribute("analisisDisponible", true);
        } catch (Exception e) {
            model.addAttribute("analisisDisponible", false);
            model.addAttribute("error", "Datos poblacionales en preparación");
        }
        return "mapas/poblacion";
    }

    @GetMapping("/economia")
    public String mapaEconomia(Model model) {
        try {
            Map<String, Object> datosEconomicos = dataWarehouseService.getAnalisisEconomico();
            model.addAttribute("datosEconomicos", datosEconomicos);
            model.addAttribute("analisisDisponible", true);
        } catch (Exception e) {
            model.addAttribute("analisisDisponible", false);
            model.addAttribute("error", "Datos económicos en preparación");
        }
        return "mapas/economia";
    }

    @GetMapping("/magnitudes")
    public String mapaMagnitudes(Model model) {
        try {
            Map<String, Object> datosMagnitudes = dataWarehouseService.getAnalisisMagnitudes();
            List<Object> sismos = sismoService.obtenerTodosLosSismos();
            
            model.addAttribute("datosMagnitudes", datosMagnitudes);
            model.addAttribute("totalSismos", sismos.size());
            model.addAttribute("analisisDisponible", true);
        } catch (Exception e) {
            model.addAttribute("analisisDisponible", false);
            model.addAttribute("error", "Análisis de magnitudes en preparación");
        }
        return "mapas/magnitudes";
    }

    @GetMapping("/riesgo-sismico")
    public String mapaRiesgoSismico(Model model) {
        try {
            Map<String, Object> datosRiesgo = dataWarehouseService.getMapaRiesgoSismico();
            model.addAttribute("datosRiesgo", datosRiesgo);
            model.addAttribute("analisisDisponible", true);
        } catch (Exception e) {
            model.addAttribute("analisisDisponible", false);
            model.addAttribute("error", "Mapa de riesgo sísmico en preparación");
        }
        return "mapas/riesgo-sismico";
    }

    @GetMapping("/tiempo-real")
    public String mapaTiempoReal(Model model) {
        try {
            List<Object> sismosRecientes = sismoService.obtenerSismosRecientes();
            model.addAttribute("sismosRecientes", sismosRecientes);
            model.addAttribute("monitoreoActivo", true);
        } catch (Exception e) {
            model.addAttribute("monitoreoActivo", false);
            model.addAttribute("error", "Monitoreo en tiempo real temporalmente no disponible");
        }
        return "mapas/tiempo-real";
    }

    // === APIs REST PARA DATOS DINÁMICOS ===

    @GetMapping("/api/estadisticas-estados")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEstadisticasEstados() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();
            
            // Datos de riesgo sísmico por estado
            Map<String, Object> datosRiesgo = dataWarehouseService.getMapaRiesgoSismico();
            Map<String, Object> datosEconomicos = dataWarehouseService.getAnalisisEconomico();
            Map<String, Object> datosPoblacion = dataWarehouseService.getAnalisisPoblacional();
            
            estadisticas.put("riesgoSismico", datosRiesgo);
            estadisticas.put("economicos", datosEconomicos);
            estadisticas.put("poblacionales", datosPoblacion);
            estadisticas.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo estadísticas: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/tendencia-temporal")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTendenciaTemporal() {
        try {
            Map<String, Object> tendencias = dataWarehouseService.getTendenciaTemporal();
            return ResponseEntity.ok(tendencias);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo tendencias: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/sismos-recientes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSismosRecientes() {
        try {
            List<Object> sismosRecientes = sismoService.obtenerSismosRecientes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("sismos", sismosRecientes);
            response.put("total", sismosRecientes.size());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo sismos recientes: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/api/datos-mapa/{tipo}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDatosMapa(@PathVariable String tipo) {
        try {
            Map<String, Object> datos = new HashMap<>();
            
            switch (tipo.toLowerCase()) {
                case "poblacion":
                    datos = dataWarehouseService.getAnalisisPoblacional();
                    break;
                case "economia":
                    datos = dataWarehouseService.getAnalisisEconomico();
                    break;
                case "magnitudes":
                    datos = dataWarehouseService.getAnalisisMagnitudes();
                    break;
                case "riesgo":
                    datos = dataWarehouseService.getMapaRiesgoSismico();
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de mapa no válido: " + tipo);
            }
            
            return ResponseEntity.ok(datos);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error obteniendo datos del mapa " + tipo + ": " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // === MÉTODOS DE UTILIDAD ===

    private Map<String, Object> getDatosEstadosPorDefecto() {
        Map<String, Object> datos = new HashMap<>();
        
        // Datos por defecto para cuando el DataWarehouse no esté disponible
        datos.put("totalEstados", 32);
        datos.put("estadosAltoRiesgo", 8);
        datos.put("estadosRiesgoModerado", 12);
        datos.put("estadosBajoRiesgo", 12);
        datos.put("mensaje", "Datos del sistema básico");
        
        return datos;
    }
}