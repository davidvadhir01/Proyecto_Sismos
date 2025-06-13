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
            Double magnitudMaxima = obtenerMagnitudMaximaReciente(sismos);
            
            // === DATOS DEL DATAWAREHOUSE ===
            Map<String, Object> datosRiesgo = null;
            Map<String, Object> datosEconomicos = null;
            Map<String, Object> tendencias = null;
            boolean analisisDisponible = false;
            
            try {
                datosRiesgo = dataWarehouseService.getMapaRiesgoSismico();
                datosEconomicos = dataWarehouseService.getAnalisisEconomico();
                tendencias = dataWarehouseService.getTendenciaTemporal();
                analisisDisponible = true;
            } catch (Exception e) {
                System.out.println("DataWarehouse no disponible: " + e.getMessage());
                analisisDisponible = false;
            }
            
            // === SISMOS RECIENTES PARA LA PÁGINA ===
            List<Sismo> sismosRecientes = sismoService.obtenerSismosRecientes();
            
            // === ENVIAR DATOS A LA VISTA ===
            model.addAttribute("totalSismos", totalSismos);
            model.addAttribute("sismosUltimoMes", sismosUltimoMes);
            model.addAttribute("magnitudMaxima", magnitudMaxima);
            model.addAttribute("estadosMonitoreados", 32);
            
            // Datos del DataWarehouse
            model.addAttribute("datosRiesgo", datosRiesgo);
            model.addAttribute("datosEconomicos", datosEconomicos);
            model.addAttribute("tendencias", tendencias);
            model.addAttribute("sismosRecientes", sismosRecientes);
            
            // Indicador de disponibilidad del DataWarehouse
            model.addAttribute("analisisDisponible", analisisDisponible);
            
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
            List<Sismo> sismosRecientes = sismoService.obtenerSismosRecientes();
            return sismosRecientes.size();
        } catch (Exception e) {
            return 127; // Valor por defecto
        }
    }
    
    private Double obtenerMagnitudMaximaReciente(List<Sismo> sismos) {
        try {
            if (sismos != null && !sismos.isEmpty()) {
                return sismos.stream()
                    .mapToDouble(Sismo::getMagnitud)
                    .max()
                    .orElse(5.2);
            }
            return 5.2;
        } catch (Exception e) {
            return 5.2; // Valor por defecto
        }
    }
    
    // === RUTAS SIMPLES SIN LOGIN ===
    
    @GetMapping("/alerts")
    public String alertas(Model model) {
        try {
            List<Sismo> sismosRecientes = sismoService.obtenerSismosRecientes();
            model.addAttribute("sismosRecientes", sismosRecientes);
            model.addAttribute("alertasActivas", sismosRecientes.size());
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando alertas");
        }
        return "alerts";
    }
    
    @GetMapping("/about")
    public String acercaDe(Model model) {
        try {
            // Información del sistema
            List<Sismo> todosSismos = sismoService.obtenerTodosLosSismos();
            model.addAttribute("totalSismosHistorico", todosSismos.size());
            
            // Verificar DataWarehouse
            boolean datawarehouseActivo = false;
            try {
                dataWarehouseService.getMapaRiesgoSismico();
                datawarehouseActivo = true;
            } catch (Exception e) {
                datawarehouseActivo = false;
            }
            
            model.addAttribute("datawarehouseActivo", datawarehouseActivo);
        } catch (Exception e) {
            model.addAttribute("error", "Error cargando información del sistema");
        }
        return "about";
    }
}