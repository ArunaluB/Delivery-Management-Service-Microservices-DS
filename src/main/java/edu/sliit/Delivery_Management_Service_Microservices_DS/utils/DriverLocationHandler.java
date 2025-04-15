package edu.sliit.Delivery_Management_Service_Microservices_DS.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.DriverLocationDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DriverLocationHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DriverLocationHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DriverService driverService;

    public DriverLocationHandler(DriverService driverService) {
        this.driverService = driverService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // Parse incoming message
            DriverLocationDto locationUpdate = objectMapper.readValue(
                    message.getPayload(),
                    DriverLocationDto.class
            );

            logger.info("Received location update from driver {}: {},{}",
                    locationUpdate.getDriverId(),
                    locationUpdate.getLatitude(),
                    locationUpdate.getLongitude()
            );

            driverService.updateOrderStatusComplted(
                    locationUpdate.getDriverId(),
                    locationUpdate.getLatitude(),
                    locationUpdate.getLongitude()
            );

        } catch (Exception e) {
            logger.error("Error processing location update: {}", e.getMessage());
            handleError(session, "Invalid location format");
        }
    }

    private void handleError(WebSocketSession session, String errorMessage) {
        try {
            session.sendMessage(new TextMessage("ERROR: " + errorMessage));
        } catch (Exception e) {
            logger.error("Failed to send error message: {}", e.getMessage());
        }
    }
}