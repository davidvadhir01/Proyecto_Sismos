package com.spring.proyectofinal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si los datos ya están cargados
        if (isDataAlreadyLoaded()) {
            System.out.println("📊 Los datos del DataWarehouse ya están cargados.");
            printDataSummary();
            return;
        }

        System.out.println("🚀 Iniciando carga de datos del DataWarehouse...");
        
        try {
            // Cargar datos en orden de dependencias
            loadDimensionData();
            loadSismosData(); // ¡IMPORTANTE: Cargar sismos!
            loadFactData();
            
            System.out.println("✅ Carga de datos del DataWarehouse completada exitosamente.");
            printDataSummary();
            
        } catch (Exception e) {
            System.err.println("❌ Error cargando datos del DataWarehouse: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isDataAlreadyLoaded() {
        try {
            Integer countZonas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dim_zonas", Integer.class);
            Integer countSismos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sismos", Integer.class);
            
            // Considerar cargado si hay datos en ambas tablas principales
            return countZonas != null && countZonas > 0 && countSismos != null && countSismos > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    private void loadDimensionData() throws IOException {
        System.out.println("📊 Cargando dimensiones del DataWarehouse...");
        
        // Cargar dimensiones en orden
        loadSQLFromResource("data/dim_zonas.sql");
        loadSQLFromResource("data/dim_economia.sql");
        loadSQLFromResource("data/dim_tiempo.sql");
    }

    @Transactional
    private void loadSismosData() throws IOException {
        System.out.println("🌍 Cargando datos de sismos...");
        
        // Intentar cargar archivo de sismos
        try {
            loadSQLFromResource("data/sismos.sql");
        } catch (Exception e) {
            System.err.println("⚠️ No se encontró data/sismos.sql, generando datos simulados...");
            generateSampleSismosData();
        }
    }

    @Transactional
    private void loadFactData() throws IOException {
        System.out.println("📈 Cargando tabla de hechos...");
        
        try {
            loadSQLFromResource("data/fact_impacto_sismos.sql");
        } catch (Exception e) {
            System.err.println("⚠️ No se encontró data/fact_impacto_sismos.sql, generando datos calculados...");
            generateFactData();
        }
    }

    private void loadSQLFromResource(String resourcePath) throws IOException {
        System.out.println("🔄 Cargando: " + resourcePath.replace("data/", "").replace(".sql", "").toUpperCase());
        
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                List<String> statements = parseSQLStatements(reader);
                int successCount = 0;
                
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        try {
                            jdbcTemplate.execute(statement);
                            successCount++;
                        } catch (Exception e) {
                            System.err.println("❌ Error ejecutando statement: " + e.getMessage());
                            // Solo mostrar los primeros 100 caracteres del statement problemático
                            String preview = statement.length() > 100 ? 
                                statement.substring(0, 100) + "..." : statement;
                            System.err.println("📝 Statement: " + preview);
                        }
                    }
                }
                
                System.out.println("✅ " + resourcePath.replace("data/", "").replace(".sql", "").toUpperCase() + " completado:");
                System.out.println("   - Statements exitosos: " + successCount);
            }
        } catch (Exception e) {
            System.err.println("❌ Error leyendo archivo: " + resourcePath);
            throw e;
        }
    }

    private List<String> parseSQLStatements(BufferedReader reader) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            // Ignorar comentarios y líneas vacías
            if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) {
                continue;
            }
            
            currentStatement.append(line).append(" ");
            
            // Si la línea termina con ';', es el final de un statement
            if (line.endsWith(";")) {
                String statement = currentStatement.toString().trim();
                if (!statement.isEmpty()) {
                    // Remover el ';' final
                    statement = statement.substring(0, statement.length() - 1);
                    statements.add(statement);
                }
                currentStatement = new StringBuilder();
            }
        }
        
        // Agregar el último statement si no termina con ';'
        String lastStatement = currentStatement.toString().trim();
        if (!lastStatement.isEmpty()) {
            statements.add(lastStatement);
        }
        
        return statements;
    }

    // Generar datos de sismos simulados si no existe el archivo
    private void generateSampleSismosData() {
        System.out.println("🎲 Generando datos de sismos simulados...");
        
        try {
            String[] estados = {"OAX", "GRO", "CHIS", "MICH", "JAL", "COL", "PUE", "VER", "CDMX", "MEX"};
            String[] referencias = {
                "95 km al N de Iguala, GRO",
                "78 km al SO de Acapulco, GRO", 
                "45 km al SE de Oaxaca, OAX",
                "23 km al E de Colima, COL",
                "67 km al NE de Tapachula, CHIS"
            };
            
            // Generar 1000 sismos simulados para empezar
            for (int i = 0; i < 1000; i++) {
                String estado = estados[(int)(Math.random() * estados.length)];
                String referencia = referencias[(int)(Math.random() * referencias.length)];
                double magnitud = 2.5 + Math.random() * 5.0; // 2.5 a 7.5
                double latitud = 14.0 + Math.random() * 18.0; // Rango de México
                double longitud = -118.0 + Math.random() * 28.0; // Rango de México
                double profundidad = 5.0 + Math.random() * 200.0;
                
                // Fecha aleatoria en los últimos 5 años
                String fecha = String.format("'202%d-%02d-%02d 00:00:00'", 
                    (int)(Math.random() * 5), 
                    1 + (int)(Math.random() * 12),
                    1 + (int)(Math.random() * 28));
                
                String horaUtc = String.format("'%02d:%02d:%02d'",
                    (int)(Math.random() * 24),
                    (int)(Math.random() * 60),
                    (int)(Math.random() * 60));
                
                String sql = String.format(
                    "INSERT IGNORE INTO sismos (fecha, magnitud, latitud, longitud, profundidad, referencia, estado, hora_utc) VALUES (%s, %.2f, %.4f, %.4f, %.1f, '%s', '%s', %s)",
                    fecha, magnitud, latitud, longitud, profundidad, referencia, estado, horaUtc
                );
                
                jdbcTemplate.execute(sql);
            }
            
            System.out.println("✅ Generados 1000 sismos simulados");
            
        } catch (Exception e) {
            System.err.println("❌ Error generando datos simulados de sismos: " + e.getMessage());
        }
    }

    // Generar tabla de hechos basada en los datos existentes
    private void generateFactData() {
        System.out.println("🧮 Generando tabla de hechos calculada...");
        
        try {
            String sql = """
                INSERT IGNORE INTO fact_impacto_sismos_imputed 
                (ID_sismo, ID_zonas, ID_economia, ID_tiempo, poblacion_afectada, impacto_economico, determinante_riesgo, riesgo_proporcional, indice_zscore)
                SELECT 
                    s.id,
                    z.ID_zonas,
                    e.ID_economia,
                    t.ID_tiempo,
                    ROUND(z.pobtot * (s.magnitud / 10) * 0.1) as poblacion_afectada,
                    ROUND(e.produccion_bruta_total * (s.magnitud / 10) * 0.001, 2) as impacto_economico,
                    ROUND((s.magnitud / 9.0) + RAND() * 0.3, 5) as determinante_riesgo,
                    ROUND((s.magnitud / 9.0) * (z.pobtot / 16992418), 5) as riesgo_proporcional,
                    ROUND((s.magnitud - 4.2) / 1.5, 5) as indice_zscore
                FROM sismos s
                JOIN dim_zonas z ON (
                    CASE 
                        WHEN s.estado = 'CDMX' THEN z.nom_ent = 'Ciudad de Mexico'
                        WHEN s.estado = 'MEX' THEN z.nom_ent = 'Mexico'
                        WHEN s.estado = 'JAL' THEN z.nom_ent = 'Jalisco'
                        WHEN s.estado = 'OAX' THEN z.nom_ent = 'Oaxaca'
                        WHEN s.estado = 'GRO' THEN z.nom_ent = 'Guerrero'
                        WHEN s.estado = 'CHIS' THEN z.nom_ent = 'Chiapas'
                        WHEN s.estado = 'VER' THEN z.nom_ent = 'Veracruz'
                        WHEN s.estado = 'MICH' THEN z.nom_ent = 'Michoacan'
                        WHEN s.estado = 'PUE' THEN z.nom_ent = 'Puebla'
                        WHEN s.estado = 'COL' THEN z.nom_ent = 'Colima'
                        ELSE z.nom_ent = 'Oaxaca'
                    END
                )
                JOIN dim_economia e ON e.nombre_entidad = z.nom_ent
                JOIN dim_tiempo t ON DATE(t.fecha) = DATE(s.fecha)
                LIMIT 10000
                """;
            
            jdbcTemplate.execute(sql);
            System.out.println("✅ Tabla de hechos generada automáticamente");
            
        } catch (Exception e) {
            System.err.println("❌ Error generando tabla de hechos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printDataSummary() {
        System.out.println("📈 === RESUMEN DEL DATAWAREHOUSE ===");
        System.out.println("📊 Registros por tabla:");
        
        try {
            Integer countSismos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sismos", Integer.class);
            Integer countZonas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dim_zonas", Integer.class);
            Integer countEconomia = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dim_economia", Integer.class);
            Integer countTiempo = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dim_tiempo", Integer.class);
            Integer countHechos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM fact_impacto_sismos_imputed", Integer.class);
            
            System.out.println("   - Sismos: " + (countSismos != null ? countSismos : 0));
            System.out.println("   - Zonas: " + (countZonas != null ? countZonas : 0));
            System.out.println("   - Economía: " + (countEconomia != null ? countEconomia : 0));
            System.out.println("   - Tiempo: " + (countTiempo != null ? countTiempo : 0));
            System.out.println("   - Hechos: " + (countHechos != null ? countHechos : 0));
            
            // Verificar integridad
            if (countSismos > 0 && countZonas > 0 && countEconomia > 0 && countHechos > 0) {
                System.out.println("✅ DataWarehouse íntegro y listo para usar!");
                
                // Mostrar muestra de estados
                try {
                    List<String> estadosEjemplo = jdbcTemplate.queryForList(
                        "SELECT nom_ent FROM dim_zonas LIMIT 5", String.class);
                    System.out.println("🗺️  Estados cargados (ejemplo): " + String.join(", ", estadosEjemplo) + "...");
                } catch (Exception e) {
                    // Ignorar si no se puede obtener
                }
            } else {
                System.out.println("⚠️ DataWarehouse incompleto - faltan datos en algunas tablas");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo resumen: " + e.getMessage());
        }
    }
}