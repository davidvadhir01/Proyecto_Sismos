package com.spring.proyectofinal.util;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.spring.proyectofinal.model.Sismo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class SismosCSVReader {

    private static final Logger logger = LoggerFactory.getLogger(SismosCSVReader.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int REQUIRED_COLUMNS = 10; // Columnas esperadas según tu CSV

    public List<Sismo> readSismosFromCSV(String fileName) {
        List<Sismo> sismos = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource(fileName);

            // Configurar lector CSV para manejar comillas y comas internas
            try (var reader = new CSVReaderBuilder(new InputStreamReader(resource.getInputStream()))
                    .withCSVParser(new CSVParserBuilder()
                            .withQuoteChar('"')
                            .build())
                    .build()) {

                // 1. Saltar metadatos (4 líneas)
                skipMetadataLines(reader, 4);

                // 2. Validar encabezados (opcional)
                validateHeaders(reader.readNext());

                // 3. Procesar datos
                processDataLines(reader, sismos);

            }
        } catch (IOException | CsvValidationException e) {
            logger.error("Error crítico al leer el archivo CSV", e);
        }

        return sismos;
    }

    private void skipMetadataLines(com.opencsv.CSVReader reader, int linesToSkip) throws CsvValidationException, IOException {
        for (int i = 0; i < linesToSkip; i++) {
            reader.readNext();
        }
    }

    private void validateHeaders(String[] headers) {
        if (headers == null || headers.length < REQUIRED_COLUMNS) {
            logger.warn("Encabezados no coinciden con el formato esperado");
        }
    }

    private void processDataLines(com.opencsv.CSVReader reader, List<Sismo> sismos)
            throws CsvValidationException, IOException {

        String[] line;
        while ((line = reader.readNext()) != null) {
            try {
                if (line.length < REQUIRED_COLUMNS) {
                    logger.warn("Línea ignorada - Columnas insuficientes: {}", String.join(",", line));
                    continue;
                }

                Sismo sismo = parseSismoFromLine(line);
                if (sismo != null) {
                    sismos.add(sismo);
                }
            } catch (Exception e) {
                logger.error("Error procesando línea: {}", String.join(",", line), e);
            }
        }
    }

    private Sismo parseSismoFromLine(String[] line) {
        try {
            LocalDate fecha = LocalDate.parse(line[0], DATE_FORMATTER);
            LocalTime hora = LocalTime.parse(line[1], TIME_FORMATTER);

            Sismo sismo = new Sismo();
            sismo.setFecha(LocalDateTime.of(fecha, hora));
            sismo.setMagnitud(parseDoubleSafe(line[2]));
            sismo.setLatitud(parseDoubleSafe(line[3]));
            sismo.setLongitud(parseDoubleSafe(line[4]));
            sismo.setProfundidad(parseDoubleSafe(line[5]));
            sismo.setReferencia(cleanReference(line[6]));
            sismo.setEstado(line[9].trim()); // Estatus en posición 9

            return sismo;
        } catch (Exception e) {
            logger.error("Error parseando línea: {}", String.join(",", line), e);
            return null;
        }
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("Valor numérico inválido: {}", value);
            return 0.0; // O podrías lanzar una excepción
        }
    }

    private String cleanReference(String reference) {
        return reference.replace("\"", "").trim();
    }
}