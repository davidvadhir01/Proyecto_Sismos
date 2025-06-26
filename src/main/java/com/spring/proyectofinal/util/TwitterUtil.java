package com.spring.proyectofinal.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.net.URLEncoder;

@Component
public class TwitterUtil {

    // Tus credenciales
    private static final String BEARER_TOKEN = "AAAAAAAAAAAAAAAAAAAAAEDt2gEAAAAAcSF3BU7VEoI6%2F0jeXQP1M1MgYos%3DJba8KCCJZhHUC8O6Y8Ow7WngzgdcKJeVohCQNCLFWKya9qXDjz";
    private static final String USER_AGENT = "gabrielchochitos@gmail.com";


    private static String cachedUserId = null;
    private static long cacheExpiry = 0;

    public List<StatusMock> getLatestTweets(String username, int count) {
        try {
            String userId;

            if (cachedUserId != null && System.currentTimeMillis() < cacheExpiry) {
                userId = cachedUserId;
            } else {
                userId = getUserId(username);
                if (userId != null) {
                    cachedUserId = userId;
                    cacheExpiry = System.currentTimeMillis() + 60 * 60 * 1000; // 1 hora
                }
            }

            if (userId == null) {
                System.err.println("No se pudo encontrar el ID del usuario: " + username);
                return new ArrayList<>();
            }

            String jsonResponse = fetchUserTweets(userId, count);
            return parseTweetsFromJson(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /* 
    // Método para obtener tweets del usuario dado su username
    public List<StatusMock> getLatestTweets(String username, int count) {
        try {
            // 1. Obtener ID del usuario por su nombre de usuario
            String userId = getUserId(username);

            if (userId == null) {
                System.err.println("No se pudo encontrar el ID del usuario: " + username);
                return new ArrayList<>();
            }

            // 2. Obtener los últimos tweets del usuario
            String jsonResponse = fetchUserTweets(userId, count);

            // 3. Parsear respuesta JSON
            return parseTweetsFromJson(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }*/

    private String getUserId(String username) throws Exception {
        // Limpia y codifica el nombre de usuario
        String encodedUsername = URLEncoder.encode(username.trim(), StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twitter.com/2/users/by/username/" + encodedUsername))
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .header("User-Agent", USER_AGENT)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.println("Error al obtener ID del usuario: " + response.body());
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response.body());
        return jsonNode.get("data").get("id").asText();
    }


    private int retryCount = 0;
    private final int MAX_RETRIES = 3;

    private String fetchUserTweets(String userId, int count) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://api.twitter.com/2/users/" + userId + "/tweets?max_results=" + count;

        

        while (retryCount <= MAX_RETRIES) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + BEARER_TOKEN)
                        .header("User-Agent", USER_AGENT)
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Mostrar información sobre límites de la API
                System.out.println("Código de estado: " + response.statusCode());
                System.out.println("Requests restantes: " + response.headers().firstValue("x-rate-limit-remaining").orElse("Desconocido"));
                System.out.println("Se reinicia en: " + response.headers().firstValue("x-rate-limit-reset").orElse("Desconocido"));

                if (response.statusCode() == 429) {
                    System.out.println("Límite alcanzado. Reintentando en 10 segundos...");
                    Thread.sleep(10000); // Espera 10 segundos
                    retryCount++;
                } else if (response.statusCode() == 200) {
                    return response.body();
                } else {
                    throw new RuntimeException("Error al obtener tweets: " + response.body());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupción durante reintento", e);
            }
        }

        throw new RuntimeException("Máximo número de reintentos alcanzado");
    }


    /*
    private String fetchUserTweets(String userId, int count) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.twitter.com/2/users/" + userId + "/tweets?max_results=" + count))
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .header("User-Agent", USER_AGENT)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al obtener tweets: " + response.body());
        }

        return response.body();
    }*/

    // Parsea los tweets desde JSON a una lista de objetos mockeados
    private List<StatusMock> parseTweetsFromJson(String jsonResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dataNode = mapper.readTree(jsonResponse).get("data");

        List<StatusMock> tweets = new ArrayList<>();

        if (dataNode != null && dataNode.isArray()) {
            for (JsonNode tweetNode : dataNode) {
                StatusMock status = new StatusMock(
                        tweetNode.get("text").asText(),
                        tweetNode.get("id").asText()
                );
                tweets.add(status);
            }
        }

        return tweets;
    }

    // Clase simple para simular un Tweet (equivalente a twitter4j.Status)
    public static class StatusMock {
        private final String text;
        private final String id;

        public StatusMock(String text, String id) {
            this.text = text;
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public String getId() {
            return id;
        }
    }
}

/*
package com.spring.proyectofinal.util;

import java.util.List;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import org.springframework.stereotype.Component;

@Component
public class TwitterUtil {

    public Twitter getTwitterInstance() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("HaKVPj7XqtG3PGHa3jeCMSvHK")
          .setOAuthConsumerSecret("b6nhNsrZaGECdeSn7id4RviQvZSYdLbm7oRrqHDdfGTtQfAW7q")
          .setOAuthAccessToken("1553599912834744321-YNQatxPYyTzm3J3QxX4UkgZ9yiVC8t")
          .setOAuthAccessTokenSecret("A7O7by8io9ialnNKtgFGymhZz6744hafi9z0mAZz9ijaD");

        return new TwitterFactory(cb.build()).getInstance();
    }

    //API key: HaKVPj7XqtG3PGHa3jeCMSvHK    
    //API and secret: b6nhNsrZaGECdeSn7id4RviQvZSYdLbm7oRrqHDdfGTtQfAW7q    
    //Bearer token: AAAAAAAAAAAAAAAAAAAAAEDt2gEAAAAAcSF3BU7VEoI6%2F0jeXQP1M1MgYos%3DJba8KCCJZhHUC8O6Y8Ow7WngzgdcKJeVohCQNCLFWKya9qXDjz
    //Access token: 1553599912834744321-YNQatxPYyTzm3J3QxX4UkgZ9yiVC8t
    //Access token secret: A7O7by8io9ialnNKtgFGymhZz6744hafi9z0mAZz9ijaD

    public List<Status> getLatestTweets(String screenName, int count) throws TwitterException {
        return getTwitterInstance().getUserTimeline(screenName, new Paging(1, count));
    }
}*/