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
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-delivery")
                .setAllowedOriginPatterns("*")
                .withSockJS();

    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(driverLocationHandler, "/ws/driver-location")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler driverLocationWebSocketHandler(DriverLocationHandler handler) {
        return handler;
    }
}
