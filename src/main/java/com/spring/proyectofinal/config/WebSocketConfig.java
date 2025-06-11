// WebSocketConfig.java - Versión compatible
package com.spring.proyectofinal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar un broker simple en memoria
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefijo para destinos de aplicación
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para conectar con SockJS como fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cambié esto para evitar problemas CORS
                .withSockJS();
    }
}