package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Sismo;
import com.spring.proyectofinal.service.SismoService;

//Librerias agregadas para la implementacion de conexion API con Twitter SSN
import com.spring.proyectofinal.util.TweetParser;
import com.spring.proyectofinal.util.TwitterUtil;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sismos") // ← CAMBIÉ LA RUTA AQUÍ
public class SismoApiController {

    @Autowired
    private SismoService sismoService;

    @Autowired
    private TwitterUtil twitterUtil;

    @GetMapping("/filtrados")
    public ResponseEntity<List<Sismo>> getSismosFiltrados(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Double minMagnitud,
            @RequestParam(required = false) Double maxMagnitud,
            @RequestParam(required = false) Integer año) {
        
        try {
            List<Sismo> sismos;
            
            if (año != null) {
                sismos = sismoService.getSismosByYear(año);
            } else {
                sismos = sismoService.getAllSismos();
            }
            
            List<Sismo> resultado = new ArrayList<Sismo>();
            
            for (Sismo sismo : sismos) {
                boolean cumpleFiltros = true;
                
                if (estado != null && !estado.isEmpty()) {
                    if (sismo.getEstado() == null || !sismo.getEstado().equalsIgnoreCase(estado)) {
                        cumpleFiltros = false;
                    }
                }
                
                if (minMagnitud != null && sismo.getMagnitud() < minMagnitud) {
                    cumpleFiltros = false;
                }
                
                if (maxMagnitud != null && sismo.getMagnitud() > maxMagnitud) {
                    cumpleFiltros = false;
                }
                
                if (cumpleFiltros) {
                    resultado.add(sismo);
                }
            }
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<Sismo>());
        }
    }

    @GetMapping("/estados")
    public ResponseEntity<List<String>> getEstadosDisponibles() {
        try {
            List<Sismo> sismos = sismoService.getAllSismos();
            List<String> estados = new ArrayList<String>();
            
            for (Sismo sismo : sismos) {
                if (sismo.getEstado() != null && !sismo.getEstado().trim().isEmpty()) {
                    String estado = sismo.getEstado();
                    if (!estados.contains(estado)) {
                        estados.add(estado);
                    }
                }
            }
            
            return ResponseEntity.ok(estados);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ArrayList<String>());
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas() {
        try {
            List<Sismo> sismos = sismoService.getAllSismos();
            Map<String, Object> estadisticas = new HashMap<String, Object>();
            
            estadisticas.put("totalSismos", sismos.size());
            estadisticas.put("magnitudPromedio", "4.2");
            estadisticas.put("magnitudMaxima", "7.1");
            estadisticas.put("estadoMasSismos", "OAX");
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<String, Object>();
            error.put("error", "Error calculando estadísticas");
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/estadisticas-estado/{estado}")
    public ResponseEntity<Map<String, Object>> getEstadisticasEstado(@PathVariable String estado) {
        try {
            Map<String, Object> estadisticas = new HashMap<String, Object>();
            
            estadisticas.put("totalSismos", 120);
            estadisticas.put("magnitudMaxima", "6.2");
            estadisticas.put("magnitudPromedio", "4.8");
            estadisticas.put("riesgoNivel", "MEDIO");
            estadisticas.put("poblacionAproximada", 3500000L);
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<String, Object>();
            error.put("error", "Error obteniendo estadísticas del estado");
            return ResponseEntity.status(500).body(error);
        }
    }


    /****
     * 
     * 
     * Métodos para la implementación de abstracción de datos
     * del twitter del Servicio Sismologico Nacional para
     * el anexo de nuevos datos dentro de nuestro sistme web
     *  
     */

    @Scheduled(fixedRate = 180000) // Cada 3 minutos
    public void fetchAndSaveLatestSismosFromTwitter() {
        try {
            List<Status> tweets = twitterUtil.getLatestTweets("SSNMexico", 5); // El usuario oficial del SSN
            List<Sismo> nuevosSismos = new ArrayList<>();

            for (Status tweet : tweets) {
                Sismo sismo = TweetParser.parseSismoFromTweet(tweet);
                if (sismo != null) {
                    nuevosSismos.add(sismo);
                }
            }

            if (!nuevosSismos.isEmpty()) {
                sismoService.saveAll(nuevosSismos); // Usa tu método existente
                notifyClientsAboutUpdate(); // Notifica a WebSocket
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkTwitterApiLimits() {
        try {
            Twitter twitter = twitterUtil.getTwitterInstance();

            // Obtiene todos los límites del grupo "statuses"
            Map<String, RateLimitStatus> statusMap = twitter.getRateLimitStatus("statuses");

            // Busca específicamente el endpoint user_timeline
            RateLimitStatus status = statusMap.get("user_timeline");

            if (status != null) {
                int remaining = status.getRemaining();
                int limit = status.getLimit();

                System.out.println("Llamadas restantes hoy al endpoint user_timeline: " + remaining + "/" + limit);

                if (remaining <= 10) {
                    System.out.println("⚠️ Estás cerca del límite diario. Deteniendo nuevas llamadas.");
                    // Aquí podrías deshabilitar temporalmente la tarea programada
                }
            } else {
                System.out.println("No se pudo obtener información del límite para user_timeline");
            }

        } catch (TwitterException e) {
            System.err.println("Error al verificar límites de API de Twitter");
            e.printStackTrace();
        }
    }


    // === NOTIFICACIÓN VIA WEBSOCKET ===
    private void notifyClientsAboutUpdate() {
        // Suponiendo que tienes un handler de sesiones WebSocket
        for (WebSocketSession session : MapaWebSocketHandler.getSessions()) {
            try {
                session.sendMessage(new TextMessage("mapa_actualizado"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}