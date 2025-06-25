package com.spring.proyectofinal.util;

import java.time.LocalDateTime;
import com.spring.proyectofinal.model.Sismo;
import twitter4j.Status;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetParser {

    // Ejemplo de patrones en los tweets:
    private static final Pattern MAGNITUD_PATTERN = Pattern.compile("Magnitud\\s*:\\s*(\\d+\\.\\d+)");
    private static final Pattern LATITUD_PATTERN = Pattern.compile("Latitud\\s*:\\s*(-?\\d+\\.\\d+)");
    private static final Pattern LONGITUD_PATTERN = Pattern.compile("Longitud\\s*:\\s*(-?\\d+\\.\\d+)");
    private static final Pattern FECHA_PATTERN = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern HORA_PATTERN = Pattern.compile("(\\d{2}:\\d{2}:\\d{2})");

    public static Sismo parseSismoFromTweet(Status tweet) {
        String texto = tweet.getText();

        Matcher magMatcher = MAGNITUD_PATTERN.matcher(texto);
        Matcher latMatcher = LATITUD_PATTERN.matcher(texto);
        Matcher lonMatcher = LONGITUD_PATTERN.matcher(texto);

        if (!magMatcher.find() || !latMatcher.find() || !lonMatcher.find()) {
            return null; // No contiene info válida
        }

        Sismo sismo = new Sismo();
        sismo.setMagnitud(Double.parseDouble(magMatcher.group(1)));
        sismo.setLatitud(Double.parseDouble(latMatcher.group(1)));
        sismo.setLongitud(Double.parseDouble(lonMatcher.group(1)));

        // Opcional: extraer fecha y hora
        Matcher fechaMatcher = FECHA_PATTERN.matcher(texto);
        Matcher horaMatcher = HORA_PATTERN.matcher(texto);

        if (fechaMatcher.find() && horaMatcher.find()) {
            String fechaStr = fechaMatcher.group(1);
            String horaStr = horaMatcher.group(1);
            LocalDateTime fecha = LocalDateTime.parse(fechaStr + "T" + horaStr);
            sismo.setFecha(fecha);
        } else {
            sismo.setFecha(LocalDateTime.now());
        }

        return sismo;
    }
}