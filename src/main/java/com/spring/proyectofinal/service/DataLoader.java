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
        // Solo cargar datos si las tablas están vacías
        if (isDataAlreadyLoaded()) {
            System.out.println("Los datos del DataWarehouse ya están cargados.");
            return;
        }

        System.out.println("Iniciando carga de datos del DataWarehouse...");
        
        try {
            // Cargar datos en orden de dependencias
            loadDimensionData();
            loadFactData();
            
            System.out.println("Carga de datos del DataWarehouse completada exitosamente.");
        } catch (Exception e) {
            System.err.println("Error cargando datos del DataWarehouse: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isDataAlreadyLoaded() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dim_zonas", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    private void loadDimensionData() throws IOException {
        // Cargar dimensiones en orden
        loadSQLFromResource("data/dim_zonas.sql");
        loadSQLFromResource("data/dim_economia.sql");
        loadSQLFromResource("data/dim_tiempo.sql");
        // Cargar sismos base
        loadSQLFromResource("data/sismos.sql");
    }

    @Transactional
    private void loadFactData() throws IOException {
        // Cargar tabla de hechos
        loadSQLFromResource("data/fact_impacto_sismos.sql");
    }

    private void loadSQLFromResource(String resourcePath) throws IOException {
        System.out.println("Cargando: " + resourcePath);
        
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                List<String> statements = parseSQLStatements(reader);
                
                for (String statement : statements) {
                    if (!statement.trim().isEmpty()) {
                        try {
                            jdbcTemplate.execute(statement);
                        } catch (Exception e) {
                            System.err.println("Error ejecutando statement: " + e.getMessage());
                            System.err.println("Statement: " + statement.substring(0, Math.min(100, statement.length())) + "...");
                        }
                    }
                }
                
                System.out.println("✓ " + resourcePath + " cargado exitosamente");
            }
        } catch (Exception e) {
            System.err.println("Error leyendo archivo: " + resourcePath);
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
}