package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.service.DataWarehouseService;
import com.spring.proyectofinal.service.SismoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private DataWarehouseService dataWarehouseService;
    
    @Autowired
    private SismoService sismoService;

    @GetMapping("/")
    public String index(Model model) {
        try {
            // === ESTADÍSTICAS PRINCIPALES ===
            List<Sismo> sismos = sismoService.obtenerTodosLosSismos();
            Integer totalSismos = sismos != null ? sismos.size() : 0;
            
            // Sismos del último mes
            Integer sismosUltimoMes = calcularSismosRecientes(30);
            
            // Magnitud máxima reciente
            Double magnitudMaxima = obtenerMagnitudMaximaReciente();
            
            // === DATOS DE ANÁLISIS (SUTILES) ===
            Map<String, Object> datosRiesgo = null;
            Map<String, Object> datosEconomicos = null;
            Map<String, Object> tendencias = null;
            
            try {
                datosRiesgo = dataWarehouseService.getMapaRiesgoSismico();
                datosEconomicos = dataWarehouseService.getAnalisisEconomico();
                tendencias = dataWarehouseService.getTendenciaTemporal();
            } catch (Exception e) {
                // Si hay error en DataWarehouse, continuar sin datos adicionales
                System.out.println("Datos de análisis no disponibles: " + e.getMessage());
            }
            
            // === SISMOS RECIENTES PARA LA PÁGINA ===
            List<Sismo> sismosRecientes = sismoService.obtenerSismosRecientes();
            
            // === ENVIAR DATOS A LA VISTA ===
            model.addAttribute("totalSismos", totalSismos);
            model.addAttribute("sismosUltimoMes", sismosUltimoMes);
            model.addAttribute("magnitudMaxima", magnitudMaxima);
            model.addAttribute("estadosMonitoreados", 32);
            
            // Datos de análisis (opcionales)
            model.addAttribute("datosRiesgo", datosRiesgo);
            model.addAttribute("datosEconomicos", datosEconomicos);
            model.addAttribute("tendencias", tendencias);
            model.addAttribute("sismosRecientes", sismosRecientes);
            
            // Indicadores de disponibilidad de análisis avanzados
            model.addAttribute("analisisDisponible", datosRiesgo != null);
            
        } catch (Exception e) {
            // Si hay cualquier error, mostrar datos por defecto
            model.addAttribute("totalSismos", 0);
            model.addAttribute("sismosUltimoMes", 0);
            model.addAttribute("magnitudMaxima", 0.0);
            model.addAttribute("estadosMonitoreados", 32);
            model.addAttribute("analisisDisponible", false);
            model.addAttribute("error", "Algunos datos no están disponibles temporalmente");
        }
        
        return "index";
    }
    
    // === MÉTODOS AUXILIARES ===
    
    private Integer calcularSismosRecientes(int dias) {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
            // Implementar lógica para contar sismos desde fechaLimite
            // Por ahora retornamos un valor estimado
            List<Sismo> todosSismos = sismoService.obtenerTodosLosSismos();
            return Math.min(todosSismos.size(), 127); // Máximo 127 como en el original
        } catch (Exception e) {
            return 127; // Valor por defecto
        }
    }
    
    private Double obtenerMagnitudMaximaReciente() {
        try {
            // Implementar lógica para obtener magnitud máxima reciente
            // Por ahora retornamos valor del diseño original
            return 5.2;
        } catch (Exception e) {
            return 5.2; // Valor por defecto
        }
    }
    
    // === RUTAS ADICIONALES ===
    
    // ELIMINADO: Este método está duplicado en SismoController
    // @GetMapping("/mapa")
    // public String mapaGeneral() {
    //     return "redirect:/mapas";
    // }
    
    @GetMapping("/alerts")
    public String alertas() {
        return "alerts";
    }
    
    @GetMapping("/statistics")
    public String estadisticas(Model model) {
        // Redirigir al análisis general
        return "redirect:/mapas";
    }
    
    @GetMapping("/reports")
    public String reportes() {
        return "reports";
    }
    
    @GetMapping("/about")
    public String acercaDe() {
        return "about";
    }
}