package edu.sliit.Delivery_Management_Service_Microservices_DS.controller;

import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.DriverOrderResponse;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.DriverResponseDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.OrderResponseDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    private final OrderService orderService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @PostMapping
    public ResponseEntity<String> processOrder(@RequestBody RequestComeOrderDto order) {
        String result = orderService.processOrder(order);
        return ResponseEntity.ok(result);
    }

    @MessageMapping("/driver/response")
    public void handleDriverResponse(DriverOrderResponse response) {
        logger.info("Received response from driver {} for order {}: {}",
                response.getDriverId(), response.getOrderId(), response.isAccepted());

        // Call service to handle the response
        orderService.handleDriverResponse(
                response.getOrderId(),
                response.getDriverId(),
                response.isAccepted()
        );
    }

    @MessageMapping("/orders/driver-response")
    public void handleDriverResponse(OrderResponseDto response) {
        logger.info("Received driver response for order {}: accepted={}",
                response.getId());
        orderService.updateOrderStatusComplted(response.getId(),response.getStatus());
    }
}
