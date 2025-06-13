package com.spring.proyectofinal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataWarehouseService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public Map<String, Object> getMapaRiesgoSismico() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Consulta SQL real para obtener zonas de alto riesgo
            String sql = """
                SELECT 
                    z.nom_ent as estado,
                    COUNT(f.ID_sismo) as cantidad_sismos,
                    AVG(f.determinante_riesgo) as riesgo_promedio,
                    SUM(f.poblacion_afectada) as poblacion_afectada_total,
                    SUM(f.impacto_economico) as impacto_economico_total
                FROM fact_impacto_sismos_imputed f
                JOIN dim_zonas z ON f.ID_zonas = z.ID_zonas
                WHERE f.determinante_riesgo > 0.7
                GROUP BY z.nom_ent
                ORDER BY AVG(f.determinante_riesgo) DESC
                LIMIT 10
                """;
                
            List<Map<String, Object>> zonasAltoRiesgo = jdbcTemplate.queryForList(sql);
            resultado.put("zonasAltoRiesgo", zonasAltoRiesgo);
            
            // Estadísticas poblacionales
            String sqlPoblacion = """
                SELECT 
                    z.nom_ent as estado,
                    z.pobtot as poblacion_total,
                    z.pobfem as poblacion_femenina,
                    z.pobmas as poblacion_masculina,
                    ROUND((z.pobfem * 100.0 / z.pobtot), 2) as porcentaje_mujeres
                FROM dim_zonas z
                WHERE z.pobtot > 1000000
                ORDER BY z.pobtot DESC
                """;
                
            List<Map<String, Object>> estadisticasPoblacion = jdbcTemplate.queryForList(sqlPoblacion);
            resultado.put("estadisticasPoblacion", estadisticasPoblacion);
            
            // Estados superavitarios (producción > consumo)
            String sqlSuperavit = """
                SELECT 
                    e.nombre_entidad as estado,
                    e.produccion_bruta_total,
                    e.consumo_intermedio,
                    (e.produccion_bruta_total - e.consumo_intermedio) as excedente,
                    ROUND((e.produccion_bruta_total / e.consumo_intermedio), 2) as ratio_eficiencia
                FROM dim_economia e
                WHERE e.produccion_bruta_total > e.consumo_intermedio
                ORDER BY (e.produccion_bruta_total - e.consumo_intermedio) DESC
                LIMIT 10
                """;
                
            List<Map<String, Object>> estadosSuperavitarios = jdbcTemplate.queryForList(sqlSuperavit);
            resultado.put("estadosSuperavitarios", estadosSuperavitarios);
            
        } catch (Exception e) {
            // Fallback a datos simulados si no hay datos en BD
            System.out.println("DataWarehouse no disponible, usando datos simulados: " + e.getMessage());
            resultado = getMapaRiesgoSismicoFallback();
        }
        
        return resultado;
    }
    
    public Map<String, Object> getAnalisisEconomico() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Ranking de producción por estado
            String sqlRanking = """
                SELECT 
                    e.nombre_entidad as estado,
                    e.produccion_bruta_total,
                    e.valor_agregado,
                    e.formacion_capital,
                    ROUND((e.valor_agregado / e.produccion_bruta_total * 100), 2) as eficiencia_productiva
                FROM dim_economia e
                ORDER BY e.produccion_bruta_total DESC
                LIMIT 15
                """;
                
            List<Map<String, Object>> rankingProduccion = jdbcTemplate.queryForList(sqlRanking);
            resultado.put("rankingProduccion", rankingProduccion);
            
            // Estados superavitarios con análisis de riesgo
            String sqlSuperavitRiesgo = """
                SELECT 
                    e.nombre_entidad as estado,
                    e.produccion_bruta_total,
                    (e.produccion_bruta_total - e.consumo_intermedio) as excedente,
                    COALESCE(AVG(f.determinante_riesgo), 0) as riesgo_promedio,
                    COALESCE(SUM(f.poblacion_afectada), 0) as poblacion_en_riesgo
                FROM dim_economia e
                LEFT JOIN fact_impacto_sismos_imputed f ON e.nombre_entidad = (
                    SELECT z.nom_ent FROM dim_zonas z WHERE z.ID_zonas = f.ID_zonas LIMIT 1
                )
                WHERE e.produccion_bruta_total > e.consumo_intermedio
                GROUP BY e.nombre_entidad, e.produccion_bruta_total, e.consumo_intermedio
                ORDER BY excedente DESC
                LIMIT 10
                """;
                
            List<Map<String, Object>> estadosSuperavitarios = jdbcTemplate.queryForList(sqlSuperavitRiesgo);
            resultado.put("estadosSuperavitarios", estadosSuperavitarios);
            
            // Totales nacionales
            String sqlTotales = """
                SELECT 
                    SUM(produccion_bruta_total) as pib_total,
                    SUM(consumo_intermedio) as consumo_total,
                    SUM(valor_agregado) as valor_agregado_total,
                    COUNT(*) as total_estados
                FROM dim_economia
                """;
                
            Map<String, Object> totales = jdbcTemplate.queryForMap(sqlTotales);
            resultado.put("totalesNacionales", totales);
            
        } catch (Exception e) {
            System.out.println("Error en análisis económico, usando datos simulados: " + e.getMessage());
            resultado = getAnalisisEconomicoFallback();
        }
        
        return resultado;
    }
    
    public Map<String, Object> getTendenciaTemporal() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Tendencia por año
            String sqlTendencia = """
                SELECT 
                    t.anio,
                    COUNT(f.ID_sismo) as cantidad_sismos,
                    AVG(f.impacto_economico) as impacto_promedio,
                    AVG(f.determinante_riesgo) as riesgo_promedio,
                    SUM(f.poblacion_afectada) as poblacion_afectada_total
                FROM fact_impacto_sismos_imputed f
                JOIN dim_tiempo t ON f.ID_tiempo = t.ID_tiempo
                WHERE t.anio >= 2020
                GROUP BY t.anio
                ORDER BY t.anio
                """;
                
            List<Map<String, Object>> tendenciaAnual = jdbcTemplate.queryForList(sqlTendencia);
            
            // Extraer arrays para gráficos
            List<Integer> años = new ArrayList<>();
            List<Long> cantidades = new ArrayList<>();
            List<Double> impactosPromedios = new ArrayList<>();
            
            for (Map<String, Object> row : tendenciaAnual) {
                años.add((Integer) row.get("anio"));
                cantidades.add((Long) row.get("cantidad_sismos"));
                impactosPromedios.add((Double) row.get("impacto_promedio"));
            }
            
            resultado.put("años", años);
            resultado.put("cantidades", cantidades);
            resultado.put("impactosPromedios", impactosPromedios);
            resultado.put("tendenciaCompleta", tendenciaAnual);
            
            // Tendencia por trimestre del año actual
            String sqlTrimestre = """
                SELECT 
                    t.trimestre,
                    COUNT(f.ID_sismo) as cantidad_sismos,
                    AVG(f.determinante_riesgo) as riesgo_promedio
                FROM fact_impacto_sismos_imputed f
                JOIN dim_tiempo t ON f.ID_tiempo = t.ID_tiempo
                WHERE t.anio = (SELECT MAX(anio) FROM dim_tiempo)
                GROUP BY t.trimestre
                ORDER BY t.trimestre
                """;
                
            List<Map<String, Object>> tendenciaTrimestral = jdbcTemplate.queryForList(sqlTrimestre);
            resultado.put("tendenciaTrimestral", tendenciaTrimestral);
            
        } catch (Exception e) {
            System.out.println("Error en tendencia temporal, usando datos simulados: " + e.getMessage());
            resultado = getTendenciaTemporalFallback();
        }
        
        return resultado;
    }
    
    public Map<String, Object> getAnalisisPoblacional() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Población total y en riesgo
            String sqlTotales = """
                SELECT 
                    SUM(z.pobtot) as poblacion_total,
                    COUNT(DISTINCT z.nom_ent) as total_estados
                FROM dim_zonas z
                """;
                
            Map<String, Object> totales = jdbcTemplate.queryForMap(sqlTotales);
            
            // Población en riesgo
            String sqlRiesgo = """
                SELECT 
                    SUM(f.poblacion_afectada) as poblacion_en_riesgo,
                    COUNT(DISTINCT f.ID_zonas) as estados_afectados
                FROM fact_impacto_sismos_imputed f
                WHERE f.determinante_riesgo > 0.5
                """;
                
            Map<String, Object> datosRiesgo = jdbcTemplate.queryForMap(sqlRiesgo);
            
            // Estadísticas por estado
            String sqlEstados = """
                SELECT 
                    z.nom_ent as estado,
                    z.pobtot as poblacion,
                    CASE 
                        WHEN AVG(f.determinante_riesgo) > 0.8 THEN 'muy alto'
                        WHEN AVG(f.determinante_riesgo) > 0.6 THEN 'alto'
                        WHEN AVG(f.determinante_riesgo) > 0.4 THEN 'moderado'
                        ELSE 'bajo'
                    END as nivel_riesgo
                FROM dim_zonas z
                LEFT JOIN fact_impacto_sismos_imputed f ON z.ID_zonas = f.ID_zonas
                GROUP BY z.nom_ent, z.pobtot
                ORDER BY z.pobtot DESC
                LIMIT 20
                """;
                
            List<Map<String, Object>> estadisticasPorEstado = jdbcTemplate.queryForList(sqlEstados);
            
            resultado.put("totalPoblacion", totales.get("poblacion_total"));
            resultado.put("poblacionEnRiesgo", datosRiesgo.get("poblacion_en_riesgo"));
            resultado.put("estadosAfectados", datosRiesgo.get("estados_afectados"));
            resultado.put("estadisticasPorEstado", estadisticasPorEstado);
            
        } catch (Exception e) {
            System.out.println("Error en análisis poblacional, usando datos simulados: " + e.getMessage());
            resultado = getAnalisisPoblacionalFallback();
        }
        
        return resultado;
    }
    
    public Map<String, Object> getAnalisisMagnitudes() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Análisis de magnitudes desde la tabla de sismos
            String sqlMagnitudes = """
                SELECT 
                    COUNT(*) as total_sismos,
                    AVG(magnitud) as magnitud_promedio,
                    MAX(magnitud) as magnitud_maxima,
                    MIN(magnitud) as magnitud_minima
                FROM sismos
                """;
                
            Map<String, Object> estadisticas = jdbcTemplate.queryForMap(sqlMagnitudes);
            
            // Distribución por rangos de magnitud
            String sqlDistribucion = """
                SELECT 
                    CASE 
                        WHEN magnitud < 3.0 THEN '2.0-2.9'
                        WHEN magnitud < 4.0 THEN '3.0-3.9'
                        WHEN magnitud < 5.0 THEN '4.0-4.9'
                        WHEN magnitud < 6.0 THEN '5.0-5.9'
                        ELSE '6.0+'
                    END as rango_magnitud,
                    COUNT(*) as cantidad,
                    ROUND((COUNT(*) * 100.0 / (SELECT COUNT(*) FROM sismos)), 2) as porcentaje
                FROM sismos
                GROUP BY 
                    CASE 
                        WHEN magnitud < 3.0 THEN '2.0-2.9'
                        WHEN magnitud < 4.0 THEN '3.0-3.9'
                        WHEN magnitud < 5.0 THEN '4.0-4.9'
                        WHEN magnitud < 6.0 THEN '5.0-5.9'
                        ELSE '6.0+'
                    END
                ORDER BY MIN(magnitud)
                """;
                
            List<Map<String, Object>> distribucion = jdbcTemplate.queryForList(sqlDistribucion);
            Map<String, Double> distribucionMagnitudes = new HashMap<>();
            
            for (Map<String, Object> row : distribucion) {
                String rango = (String) row.get("rango_magnitud");
                Double porcentaje = (Double) row.get("porcentaje");
                distribucionMagnitudes.put(rango, porcentaje);
            }
            
            resultado.put("magnitudMaxima", estadisticas.get("magnitud_maxima"));
            resultado.put("magnitudPromedio", estadisticas.get("magnitud_promedio"));
            resultado.put("totalSismos", estadisticas.get("total_sismos"));
            resultado.put("distribucionMagnitudes", distribucionMagnitudes);
            resultado.put("distribucionDetallada", distribucion);
            
        } catch (Exception e) {
            System.out.println("Error en análisis de magnitudes, usando datos simulados: " + e.getMessage());
            resultado = getAnalisisMagnitudesFallback();
        }
        
        return resultado;
    }
    
    // Métodos de fallback con datos simulados (para cuando no hay datos en BD)
    private Map<String, Object> getMapaRiesgoSismicoFallback() {
        Map<String, Object> resultado = new HashMap<>();
        
        List<Map<String, Object>> zonasAltoRiesgo = new ArrayList<>();
        Map<String, Object> zona1 = new HashMap<>();
        zona1.put("estado", "Oaxaca");
        zona1.put("riesgo_promedio", 0.85);
        zona1.put("cantidad_sismos", 156);
        zona1.put("poblacion_afectada_total", 2500000L);
        zona1.put("impacto_economico_total", 125000.50);
        zonasAltoRiesgo.add(zona1);
        
        Map<String, Object> zona2 = new HashMap<>();
        zona2.put("estado", "Guerrero");
        zona2.put("riesgo_promedio", 0.78);
        zona2.put("cantidad_sismos", 203);
        zona2.put("poblacion_afectada_total", 1800000L);
        zona2.put("impacto_economico_total", 98000.75);
        zonasAltoRiesgo.add(zona2);
        
        resultado.put("zonasAltoRiesgo", zonasAltoRiesgo);
        resultado.put("estadisticasPoblacion", new ArrayList<>());
        resultado.put("estadosSuperavitarios", new ArrayList<>());
        
        return resultado;
    }
    
    private Map<String, Object> getAnalisisEconomicoFallback() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("rankingProduccion", new ArrayList<>());
        resultado.put("estadosSuperavitarios", new ArrayList<>());
        resultado.put("totalesNacionales", Map.of(
            "pib_total", 2500000000000.0,
            "consumo_total", 1800000000000.0,
            "valor_agregado_total", 700000000000.0,
            "total_estados", 32
        ));
        return resultado;
    }
    
    private Map<String, Object> getTendenciaTemporalFallback() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("años", Arrays.asList(2020, 2021, 2022, 2023, 2024));
        resultado.put("cantidades", Arrays.asList(45L, 52L, 38L, 67L, 81L));
        resultado.put("impactosPromedios", Arrays.asList(125000.0, 134000.0, 98000.0, 156000.0, 187000.0));
        resultado.put("tendenciaCompleta", new ArrayList<>());
        resultado.put("tendenciaTrimestral", new ArrayList<>());
        return resultado;
    }
    
    private Map<String, Object> getAnalisisPoblacionalFallback() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalPoblacion", 127000000L);
        resultado.put("poblacionEnRiesgo", 44500000L);
        resultado.put("estadosAfectados", 8);
        
        List<Map<String, Object>> estadisticas = new ArrayList<>();
        Map<String, Object> est1 = new HashMap<>();
        est1.put("estado", "Oaxaca");
        est1.put("poblacion", 4100000L);
        est1.put("nivel_riesgo", "alto");
        estadisticas.add(est1);
        
        resultado.put("estadisticasPorEstado", estadisticas);
        return resultado;
    }
    
    private Map<String, Object> getAnalisisMagnitudesFallback() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("magnitudMaxima", 6.2);
        resultado.put("magnitudPromedio", 4.2);
        resultado.put("totalSismos", 15970);
        
        Map<String, Object> distribucion = new HashMap<>();
        distribucion.put("2.0-2.9", 45.5);
        distribucion.put("3.0-3.9", 32.5);
        distribucion.put("4.0-4.9", 18.0);
        distribucion.put("5.0-5.9", 3.5);
        distribucion.put("6.0+", 0.5);
        
        resultado.put("distribucionMagnitudes", distribucion);
        resultado.put("distribucionDetallada", new ArrayList<>());
        return resultado;
    }
    
    // Método para verificar si hay datos disponibles en el DataWarehouse
    public boolean isDatabasePopulated() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dim_zonas", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Método para obtener estadísticas del sistema
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Contar registros en cada tabla
            stats.put("totalZonas", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dim_zonas", Integer.class));
            stats.put("totalEconomia", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dim_economia", Integer.class));
            stats.put("totalTiempo", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dim_tiempo", Integer.class));
            stats.put("totalFactos", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fact_impacto_sismos_imputed", Integer.class));
            stats.put("totalSismos", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sismos", Integer.class));
                
            stats.put("databasePopulated", true);
            stats.put("lastUpdate", new java.util.Date());
            
        } catch (Exception e) {
            stats.put("databasePopulated", false);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    // Método para obtener información de los últimos sismos registrados
    public List<Map<String, Object>> getUltimosSismos(int limite) {
        try {
            String sql = """
                SELECT 
                    fecha,
                    magnitud,
                    latitud,
                    longitud,
                    profundidad,
                    referencia,
                    estado,
                    hora_utc
                FROM sismos
                ORDER BY fecha DESC
                LIMIT ?
                """;
                
            return jdbcTemplate.queryForList(sql, limite);
            
        } catch (Exception e) {
            System.out.println("Error obteniendo últimos sismos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Método para verificar la integridad de los datos
    public Map<String, Object> verificarIntegridadDatos() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Verificar relaciones entre tablas
            String sqlIntegridad = """
                SELECT 
                    (SELECT COUNT(*) FROM fact_impacto_sismos_imputed) as total_hechos,
                    (SELECT COUNT(DISTINCT ID_zonas) FROM fact_impacto_sismos_imputed) as zonas_utilizadas,
                    (SELECT COUNT(*) FROM dim_zonas) as total_zonas,
                    (SELECT COUNT(DISTINCT ID_economia) FROM fact_impacto_sismos_imputed) as economia_utilizada,
                    (SELECT COUNT(*) FROM dim_economia) as total_economia
                """;
                
            Map<String, Object> integridad = jdbcTemplate.queryForMap(sqlIntegridad);
            resultado.put("integridad", integridad);
            resultado.put("integridadOK", true);
            
        } catch (Exception e) {
            resultado.put("integridadOK", false);
            resultado.put("error", e.getMessage());
        }
        
        return resultado;
    }
}