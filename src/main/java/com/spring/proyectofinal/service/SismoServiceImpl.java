package com.spring.proyectofinal.service;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.repository.SismoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SismoServiceImpl implements SismoService {

    @Autowired
    private SismoRepository sismoRepository;

    @Override
    public List<Sismo> obtenerTodosLosSismos() {
        return sismoRepository.findAll();
    }

    @Override
    public Optional<Sismo> obtenerSismoPorId(Long id) {
        return sismoRepository.findById(id);
    }

    @Override
    public void eliminarSismo(Long id) {
        sismoRepository.deleteById(id);
    }

    @Override
    public Sismo save(Sismo sismo) {
        return sismoRepository.save(sismo);
    }

    @Override
    public List<Sismo> saveAll(List<Sismo> sismos) {
        return sismoRepository.saveAll(sismos);
    }

    @Override
    public List<Sismo> obtenerSismosPorAño(int año) {
        return sismoRepository.findByYear(año);
    }

    @Override
    @Cacheable("availableYears")
    public List<Integer> getAvailableYears() {
        return sismoRepository.findDistinctYears();
    }

    @Override
    public List<Sismo> getSismosByYear(Integer year) {
        if (year == null) {
            return getAllSismos();
        }
        return obtenerSismosPorAño(year);
    }

    @Override
    public List<Sismo> getAllSismos() {
        return obtenerTodosLosSismos();
    }

    @Override
    public List<Sismo> obtenerSismosRecientes() {
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        return sismoRepository.findByFechaAfter(hace30Dias);
    }
    
    // === NUEVOS MÉTODOS OPTIMIZADOS ===
    
    /**
     * Obtener sismos con filtros optimizado para el mapa
     */
    public List<Sismo> obtenerSismosFiltrados(String estado, Double minMagnitud, 
                                              Double maxMagnitud, Integer año, int limite) {
        
        List<Sismo> sismos;
        
        if (año != null) {
            sismos = obtenerSismosPorAño(año);
        } else {
            // Para evitar cargar todos los sismos, usar solo los recientes
            sismos = obtenerSismosRecientesLimitados(limite * 2); // Doble para filtrar después
        }
        
        // Aplicar filtros progresivamente para optimizar
        return sismos.stream()
            .filter(sismo -> estado == null || estado.isEmpty() || 
                    (sismo.getEstado() != null && sismo.getEstado().equalsIgnoreCase(estado)))
            .filter(sismo -> minMagnitud == null || sismo.getMagnitud() >= minMagnitud)
            .filter(sismo -> maxMagnitud == null || sismo.getMagnitud() <= maxMagnitud)
            .sorted((s1, s2) -> s2.getFecha().compareTo(s1.getFecha())) // Más recientes primero
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener estadísticas rápidas sin cargar todos los sismos
     */
    @Cacheable("estadisticasBasicas")
    public Map<String, Object> obtenerEstadisticasBasicas(String estado, Integer año) {
        List<Sismo> sismos;
        
        if (año != null && estado != null && !estado.isEmpty()) {
            sismos = obtenerSismosPorAñoYEstado(año, estado);
        } else if (año != null) {
            sismos = obtenerSismosPorAño(año);
        } else if (estado != null && !estado.isEmpty()) {
            sismos = obtenerSismosPorEstado(estado);
        } else {
            // Para evitar cargar todos los sismos, limitamos a los últimos 10000
            sismos = obtenerSismosRecientesLimitados(10000);
        }
        
        return calcularEstadisticas(sismos);
    }
    
    /**
     * Obtener todos los estados únicos de forma optimizada
     */
    @Cacheable("estadosDisponibles")
    public List<String> obtenerEstadosDisponibles() {
        return sismoRepository.findAll().stream()
            .map(Sismo::getEstado)
            .filter(estado -> estado != null && !estado.trim().isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    // === MÉTODOS AUXILIARES PRIVADOS ===
    
    private List<Sismo> obtenerSismosPorAñoLimitado(int año, String estado, 
                                                   Double minMagnitud, Double maxMagnitud, int limite) {
        return obtenerSismosPorAño(año).stream()
            .filter(sismo -> estado == null || estado.isEmpty() || 
                    (sismo.getEstado() != null && sismo.getEstado().equalsIgnoreCase(estado)))
            .filter(sismo -> minMagnitud == null || sismo.getMagnitud() >= minMagnitud)
            .filter(sismo -> maxMagnitud == null || sismo.getMagnitud() <= maxMagnitud)
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    private List<Sismo> obtenerSismosPorEstado(String estado) {
        return sismoRepository.findByEstado(estado);
    }
    
    private List<Sismo> obtenerSismosPorAñoYEstado(int año, String estado) {
        return obtenerSismosPorAño(año).stream()
            .filter(sismo -> sismo.getEstado() != null && sismo.getEstado().equalsIgnoreCase(estado))
            .collect(Collectors.toList());
    }
    
    private List<Sismo> obtenerSismosRecientesLimitados(int limite) {
        LocalDateTime hace1Año = LocalDateTime.now().minusYears(1);
        return sismoRepository.findByFechaAfter(hace1Año).stream()
            .sorted((s1, s2) -> s2.getFecha().compareTo(s1.getFecha()))
            .limit(limite)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> calcularEstadisticas(List<Sismo> sismos) {
        if (sismos.isEmpty()) {
            return Map.of(
                "totalSismos", 0,
                "magnitudPromedio", "0.00",
                "magnitudMaxima", "0.00",
                "estadoMasSismos", "N/A"
            );
        }
        
        long totalSismos = sismos.size();
        
        double magnitudPromedio = sismos.stream()
            .mapToDouble(Sismo::getMagnitud)
            .average()
            .orElse(0.0);
        
        double magnitudMaxima = sismos.stream()
            .mapToDouble(Sismo::getMagnitud)
            .max()
            .orElse(0.0);
        
        String estadoMasSismos = sismos.stream()
            .filter(s -> s.getEstado() != null && !s.getEstado().trim().isEmpty())
            .collect(Collectors.groupingBy(Sismo::getEstado, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        return Map.of(
            "totalSismos", totalSismos,
            "magnitudPromedio", String.format("%.2f", magnitudPromedio),
            "magnitudMaxima", String.format("%.2f", magnitudMaxima),
            "estadoMasSismos", estadoMasSismos
        );
    }
}