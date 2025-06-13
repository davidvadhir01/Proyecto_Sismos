package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.service.DataWarehouseService;
import com.spring.proyectofinal.service.SismoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mapas")
public class MapasController {

    private final DataWarehouseService dataWarehouseService;
    private final SismoService sismoService;

    @Autowired
    public MapasController(DataWarehouseService dataWarehouseService, SismoService sismoService) {
        this.dataWarehouseService = dataWarehouseService;
        this.sismoService = sismoService;
    }

    // === PÁGINA PRINCIPAL DE MAPAS ===
    @GetMapping
    public String mapasMenu(Model model) {
        try {
            // Verificar disponibilidad de datos del DataWarehouse
            Map<String, Object> datosRiesgo = dataWarehouseService.getMapaRiesgoSismico();
            Map<String, Object> datosEconomicos = dataWarehouseService.getAnalisisEconomico();
            Map<String, Object> datosPoblacion = dataWarehouseService.getAnalisisPoblacional();
            
            // Obtener estadísticas básicas de sismos
            List<Sismo> todosSismos = sismoService.obtenerTodosLosSismos();
            Integer totalSismos = todosSismos.size();
            List<Integer> añosDisponibles = sismoService.getAvailableYears();
            
            // Calcular estadísticas adicionales
            Double magnitudMaxima = todosSismos.stream()
                .mapToDouble(Sismo::getMagnitud)
                .max()
                .orElse(0.0);
            
            Double magnitudPromedio = todosSismos.stream()
                .mapToDouble(Sismo::getMagnitud)
                .average()
                .orElse(0.0);
            
            // Sismos por estado
            Map<String, Long> sismosPorEstado = todosSismos.stream()
                .collect(Collectors.groupingBy(
                    sismo -> sismo.getEstado() != null ? sismo.getEstado() : "Desconocido",
                    Collectors.counting()
                ));
            
            String estadoMasSismos = sismosPorEstado.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
            
            // Enviar datos a la vista
            model.addAttribute("datosDisponibles", true);
            model.addAttribute("totalSismos", totalSismos);
            model.addAttribute("magnitudMaxima", magnitudMaxima);
            model.addAttribute("magnitudPromedio", Math.round(magnitudPromedio * 100.0) / 100.0);
            model.addAttribute("estadoMasSismos", estadoMasSismos);
            model.addAttribute("añosDisponibles", añosDisponibles);
            model.addAttribute("datosRiesgo", datosRiesgo);
            model.addAttribute("datosEconomicos", datosEconomicos);
            model.addAttribute("datosPoblacion", datosPoblacion);
            model.addAttribute("sismosPorEstado", sismosPorEstado);
            
            // Estadísticas del DataWarehouse
            model.addAttribute("poblacionTotal", 127000000);
            model.addAttribute("poblacionEnRiesgo", calcularPoblacionEnRiesgo(sismosPorEstado));
            model.addAttribute("pibTotal", "2.5T");
            model.addAttribute("pibEnRiesgo", "625B");
            
        } catch (Exception e) {
            model.addAttribute("datosDisponibles", false);
            model.addAttribute("error", "DataWarehouse en mantenimiento. Funciones básicas disponibles.");
            
            // Datos básicos sin DataWarehouse
            try {
                List<Sismo> sismos = sismoService.obtenerTodosLosSismos();
                model.addAttribute("totalSismos", sismos.size());
                model.addAttribute("añosDisponibles", sismoService.getAvailableYears());
            } catch (Exception ex) {
                model.addAttribute("totalSismos", 0);
                model.addAttribute("error", "Error de conexión con la base de datos");
            }
        }
        
        return "mapas/menu";
    }

    // === MAPA DE IMPACTO POBLACIONAL ===
    @GetMapping("/poblacion")
    public String mapaPoblacion(Model model, 
                               @RequestParam(required = false) String estado,
                               @RequestParam(required = false) Double minMagnitud) {
        try {
            // Obtener datos básicos
            List<Sismo> sismos = filtrarSismos(estado, minMagnitud, null, null);
            Map<String, Object> datosPoblacion = dataWarehouseService.getAnalisisPoblacional();
            
            // Calcular impacto poblacional
            Map<String, Object> impactoPoblacional = calcularImpactoPoblacional(sismos);
            
            // Estados únicos para filtros
            List<String> estados = sismoService.obtenerTodosLosSismos().stream()
                .map(Sismo::getEstado)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            model.addAttribute("sismos", sismos);
            model.addAttribute("datosPoblacion", datosPoblacion);
            model.addAttribute("impactoPoblacional", impactoPoblacional);
            model.addAttribute("estados", estados);
            model.addAttribute("estadoSeleccionado", estado);
            model.addAttribute("minMagnitud", minMagnitud);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando análisis poblacional: " + e.getMessage());
        }
        
        return "mapas/poblacion";
    }

    // === MAPA DE IMPACTO ECONÓMICO ===
    @GetMapping("/economia")
    public String mapaEconomia(Model model,
                              @RequestParam(required = false) String estado,
                              @RequestParam(required = false) Integer año) {
        try {
            // Obtener datos económicos
            Map<String, Object> datosEconomicos = dataWarehouseService.getAnalisisEconomico();
            
            List<Sismo> sismos;
            if (año != null) {
                sismos = sismoService.getSismosByYear(año);
            } else {
                sismos = sismoService.obtenerTodosLosSismos();
            }
            
            if (estado != null && !estado.isEmpty()) {
                sismos = sismos.stream()
                    .filter(s -> estado.equals(s.getEstado()))
                    .collect(Collectors.toList());
            }
            
            // Calcular impacto económico
            Map<String, Object> impactoEconomico = calcularImpactoEconomico(sismos);
            
            // Estados únicos para filtros
            List<String> estados = sismoService.obtenerTodosLosSismos().stream()
                .map(Sismo::getEstado)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            List<Integer> años = sismoService.getAvailableYears();
            
            model.addAttribute("sismos", sismos);
            model.addAttribute("datosEconomicos", datosEconomicos);
            model.addAttribute("impactoEconomico", impactoEconomico);
            model.addAttribute("estados", estados);
            model.addAttribute("años", años);
            model.addAttribute("estadoSeleccionado", estado);
            model.addAttribute("añoSeleccionado", año);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando análisis económico: " + e.getMessage());
        }
        
        return "mapas/economia";
    }

    // === MAPA DE ANÁLISIS DE MAGNITUDES ===
    @GetMapping("/magnitudes")
    public String mapaMagnitudes(Model model,
                                @RequestParam(required = false) Double minMagnitud,
                                @RequestParam(required = false) Double maxMagnitud) {
        try {
            List<Sismo> sismos = sismoService.obtenerTodosLosSismos();
            
            // Aplicar filtros de magnitud
            if (minMagnitud != null) {
                sismos = sismos.stream()
                    .filter(s -> s.getMagnitud() >= minMagnitud)
                    .collect(Collectors.toList());
            }
            
            if (maxMagnitud != null) {
                sismos = sismos.stream()
                    .filter(s -> s.getMagnitud() <= maxMagnitud)
                    .collect(Collectors.toList());
            }
            
            // Análisis de distribución de magnitudes
            Map<String, Object> analisisMagnitudes = analizarDistribucionMagnitudes(sismos);
            
            model.addAttribute("sismos", sismos);
            model.addAttribute("analisisMagnitudes", analisisMagnitudes);
            model.addAttribute("minMagnitud", minMagnitud);
            model.addAttribute("maxMagnitud", maxMagnitud);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando análisis de magnitudes: " + e.getMessage());
        }
        
        return "mapas/magnitudes";
    }

    // === MAPA DE RIESGO SÍSMICO ===
    @GetMapping("/riesgo-sismico")
    public String mapaRiesgoSismico(Model model,
                                   @RequestParam(required = false) String nivelRiesgo) {
        try {
            Map<String, Object> datosRiesgo = dataWarehouseService.getMapaRiesgoSismico();
            List<Sismo> sismos = sismoService.obtenerTodosLosSismos();
            
            // Filtrar por nivel de riesgo
            if (nivelRiesgo != null && !nivelRiesgo.isEmpty()) {
                sismos = filtrarPorNivelRiesgo(sismos, nivelRiesgo);
            }
            
            // Calcular zonas de riesgo
            Map<String, Object> zonasRiesgo = calcularZonasRiesgo(sismos);
            
            model.addAttribute("sismos", sismos);
            model.addAttribute("datosRiesgo", datosRiesgo);
            model.addAttribute("zonasRiesgo", zonasRiesgo);
            model.addAttribute("nivelRiesgoSeleccionado", nivelRiesgo);
            model.addAttribute("nivelesRiesgo", Arrays.asList("ALTO", "MEDIO", "BAJO"));
            
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando mapa de riesgo: " + e.getMessage());
        }
        
        return "mapas/riesgo-sismico";
    }

    // === MONITOREO EN TIEMPO REAL ===
    @GetMapping("/tiempo-real")
    public String monitoreoTiempoReal(Model model) {
        try {
            // Sismos de las últimas 24 horas
            List<Sismo> sismosRecientes = sismoService.obtenerSismosRecientes();
            
            // Estadísticas en tiempo real
            Map<String, Object> estadisticasVivo = calcularEstadisticasVivo(sismosRecientes);
            
            model.addAttribute("sismosRecientes", sismosRecientes);
            model.addAttribute("estadisticasVivo", estadisticasVivo);
            model.addAttribute("ultimaActualizacion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
        } catch (Exception e) {
            model.addAttribute("error", "Error en monitoreo tiempo real: " + e.getMessage());
        }
        
        return "mapas/tiempo-real";
    }

    // === APIs REST PARA LOS MAPAS ===
    
    @GetMapping("/api/sismos-filtrados")
    @ResponseBody
    public ResponseEntity<List<Sismo>> getSismosFiltrados(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double minMagnitud,
            @RequestParam(required = false) Double maxMagnitud,
            @RequestParam(required = false) Integer año) {
        try {
            List<Sismo> sismos = filtrarSismos(estado, minMagnitud, maxMagnitud, año);
            return ResponseEntity.ok(sismos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of());
        }
    }
    
    @GetMapping("/api/estadisticas-estado/{estado}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getEstadisticasEstado(@PathVariable String estado) {
        try {
            List<Sismo> sismosEstado = sismoService.obtenerTodosLosSismos().stream()
                .filter(s -> estado.equals(s.getEstado()))
                .collect(Collectors.toList());
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalSismos", sismosEstado.size());
            estadisticas.put("magnitudMaxima", sismosEstado.stream().mapToDouble(Sismo::getMagnitud).max().orElse(0.0));
            estadisticas.put("magnitudPromedio", sismosEstado.stream().mapToDouble(Sismo::getMagnitud).average().orElse(0.0));
            estadisticas.put("poblacionAproximada", obtenerPoblacionEstado(estado));
            estadisticas.put("riesgoNivel", calcularNivelRiesgo(sismosEstado));
            
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/tendencia-temporal")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTendenciaTemporal() {
        try {
            Map<String, Object> tendencia = dataWarehouseService.getTendenciaTemporal();
            return ResponseEntity.ok(tendencia);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // === MÉTODOS AUXILIARES ===
    
    private List<Sismo> filtrarSismos(String estado, Double minMagnitud, Double maxMagnitud, Integer año) {
        List<Sismo> sismos = sismoService.obtenerTodosLosSismos();
        
        if (año != null) {
            sismos = sismoService.getSismosByYear(año);
        }
        
        return sismos.stream()
            .filter(sismo -> estado == null || estado.equals(sismo.getEstado()))
            .filter(sismo -> minMagnitud == null || sismo.getMagnitud() >= minMagnitud)
            .filter(sismo -> maxMagnitud == null || sismo.getMagnitud() <= maxMagnitud)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> calcularImpactoPoblacional(List<Sismo> sismos) {
        Map<String, Object> impacto = new HashMap<>();
        
        // Población potencialmente afectada
        long poblacionAfectada = sismos.stream()
            .mapToLong(this::estimarPoblacionAfectada)
            .sum();
        
        // Estados más afectados
        Map<String, Long> estadosAfectados = sismos.stream()
            .collect(Collectors.groupingBy(
                sismo -> sismo.getEstado() != null ? sismo.getEstado() : "Desconocido",
                Collectors.counting()
            ));
        
        impacto.put("poblacionAfectada", poblacionAfectada);
        impacto.put("estadosAfectados", estadosAfectados);
        impacto.put("totalEventos", sismos.size());
        
        return impacto;
    }
    
    private Map<String, Object> calcularImpactoEconomico(List<Sismo> sismos) {
        Map<String, Object> impacto = new HashMap<>();
        
        // Estimación de pérdidas económicas
        double perdidasEstimadas = sismos.stream()
            .mapToDouble(this::estimarPerdidasEconomicas)
            .sum();
        
        // Sectores afectados
        Map<String, Integer> sectoresAfectados = new HashMap<>();
        sectoresAfectados.put("Infraestructura", sismos.size() * 15);
        sectoresAfectados.put("Vivienda", sismos.size() * 25);
        sectoresAfectados.put("Comercio", sismos.size() * 10);
        sectoresAfectados.put("Industria", sismos.size() * 20);
        
        impacto.put("perdidasEstimadas", Math.round(perdidasEstimadas * 100.0) / 100.0);
        impacto.put("sectoresAfectados", sectoresAfectados);
        impacto.put("costoPromedioPorEvento", Math.round((perdidasEstimadas / Math.max(sismos.size(), 1)) * 100.0) / 100.0);
        
        return impacto;
    }
    
    private Map<String, Object> analizarDistribucionMagnitudes(List<Sismo> sismos) {
        Map<String, Object> analisis = new HashMap<>();
        
        // Distribución por rangos de magnitud
        Map<String, Long> distribucion = new HashMap<>();
        distribucion.put("< 3.0", sismos.stream().filter(s -> s.getMagnitud() < 3.0).count());
        distribucion.put("3.0-3.9", sismos.stream().filter(s -> s.getMagnitud() >= 3.0 && s.getMagnitud() < 4.0).count());
        distribucion.put("4.0-4.9", sismos.stream().filter(s -> s.getMagnitud() >= 4.0 && s.getMagnitud() < 5.0).count());
        distribucion.put("5.0-5.9", sismos.stream().filter(s -> s.getMagnitud() >= 5.0 && s.getMagnitud() < 6.0).count());
        distribucion.put("6.0+", sismos.stream().filter(s -> s.getMagnitud() >= 6.0).count());
        
        // Estadísticas
        DoubleSummaryStatistics stats = sismos.stream()
            .mapToDouble(Sismo::getMagnitud)
            .summaryStatistics();
        
        analisis.put("distribucion", distribucion);
        analisis.put("magnitudMaxima", stats.getMax());
        analisis.put("magnitudMinima", stats.getMin());
        analisis.put("magnitudPromedio", Math.round(stats.getAverage() * 100.0) / 100.0);
        analisis.put("totalSismos", sismos.size());
        
        return analisis;
    }
    
    private Map<String, Object> calcularZonasRiesgo(List<Sismo> sismos) {
        Map<String, Object> zonas = new HashMap<>();
        
        // Clasificar estados por nivel de riesgo
        Map<String, String> riesgoPorEstado = sismos.stream()
            .collect(Collectors.groupingBy(
                sismo -> sismo.getEstado() != null ? sismo.getEstado() : "Desconocido",
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    lista -> {
                        double promMagnitud = lista.stream().mapToDouble(Sismo::getMagnitud).average().orElse(0.0);
                        if (promMagnitud >= 5.0) return "ALTO";
                        if (promMagnitud >= 4.0) return "MEDIO";
                        return "BAJO";
                    }
                )
            ));
        
        zonas.put("riesgoPorEstado", riesgoPorEstado);
        zonas.put("estadosAltoRiesgo", riesgoPorEstado.entrySet().stream()
            .filter(entry -> "ALTO".equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList()));
        
        return zonas;
    }
    
    private Map<String, Object> calcularEstadisticasVivo(List<Sismo> sismosRecientes) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalRecientes", sismosRecientes.size());
        stats.put("mayorMagnitud", sismosRecientes.stream()
            .mapToDouble(Sismo::getMagnitud)
            .max()
            .orElse(0.0));
        stats.put("magnitudPromedio", Math.round(sismosRecientes.stream()
            .mapToDouble(Sismo::getMagnitud)
            .average()
            .orElse(0.0) * 100.0) / 100.0);
        stats.put("sismosSignificativos", sismosRecientes.stream()
            .filter(s -> s.getMagnitud() >= 4.0)
            .count());
        
        return stats;
    }
    
    // Métodos auxiliares de cálculo
    private long estimarPoblacionAfectada(Sismo sismo) {
        double factor = Math.pow(sismo.getMagnitud(), 2) * 1000;
        return Math.round(factor);
    }
    
    private double estimarPerdidasEconomicas(Sismo sismo) {
        return Math.pow(sismo.getMagnitud(), 3) * 50000; // USD
    }
    
    private List<Sismo> filtrarPorNivelRiesgo(List<Sismo> sismos, String nivel) {
        switch (nivel) {
            case "ALTO":
                return sismos.stream().filter(s -> s.getMagnitud() >= 5.0).collect(Collectors.toList());
            case "MEDIO":
                return sismos.stream().filter(s -> s.getMagnitud() >= 4.0 && s.getMagnitud() < 5.0).collect(Collectors.toList());
            case "BAJO":
                return sismos.stream().filter(s -> s.getMagnitud() < 4.0).collect(Collectors.toList());
            default:
                return sismos;
        }
    }
    
    private int obtenerPoblacionEstado(String estado) {
    Map<String, Integer> poblaciones = Map.ofEntries(
        Map.entry("CDMX", 9209944),
        Map.entry("JAL", 8348151),
        Map.entry("VER", 8062579),
        Map.entry("PUE", 6583278),
        Map.entry("GTO", 6166934),
        Map.entry("CHIS", 5543828),
        Map.entry("NL", 5784442),
        Map.entry("MICH", 4748846),
        Map.entry("OAX", 4132148),
        Map.entry("GRO", 3540685),
        Map.entry("TAM", 3527735),
        Map.entry("CHIH", 3741869)
    );
    return poblaciones.getOrDefault(estado, 1000000);
}

    private String calcularNivelRiesgo(List<Sismo> sismos) {
        double promMagnitud = sismos.stream().mapToDouble(Sismo::getMagnitud).average().orElse(0.0);
        if (promMagnitud >= 5.0) return "ALTO";
        if (promMagnitud >= 4.0) return "MEDIO";
        return "BAJO";
    }
    
    private int calcularPoblacionEnRiesgo(Map<String, Long> sismosPorEstado) {
        return sismosPorEstado.entrySet().stream()
            .mapToInt(entry -> {
                int poblacion = obtenerPoblacionEstado(entry.getKey());
                return (int) (poblacion * 0.35); // 35% en riesgo sísmico
            })
            .sum();
    }
}