package edu.sliit.Delivery_Management_Service_Microservices_DS.config;

import edu.sliit.Delivery_Management_Service_Microservices_DS.utils.DriverLocationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final DriverLocationHandler driverLocationHandler;

    public WebSocketConfig(DriverLocationHandler driverLocationHandler) {
        this.driverLocationHandler = driverLocationHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple memory-based message broker to send messages to clients
        // Prefix for messages that are bound for @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for messages that are bound for the broker (client subscribes)
        registry.enableSimpleBroker("/topic", "/queue");

        // Use /queue for user-specific messages (like order assignments)
        // Use /topic for broadcast messages
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-delivery")
                .setAllowedOriginPatterns("*")
                .withSockJS();

    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register driver location handler for raw WebSocket communication
        registry.addHandler(driverLocationHandler, "/ws/driver-location")
                .setAllowedOrigins("*");
    }

    // Complete the missing autowiring in OrderServiceImpl
    @Bean
    public WebSocketHandler driverLocationWebSocketHandler(DriverLocationHandler handler) {
        return handler;
    }
}
