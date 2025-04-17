package edu.sliit.Delivery_Management_Service_Microservices_DS.controller;

import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.*;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/driver/{driverId}/orders")
    public ResponseEntity<List<OrderDto>> getDriverOrders(@PathVariable Long driverId) {
        List<OrderDto> orders = orderService.getDeliveredOrRejectedOrdersByDriverId(driverId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/summary/driver/{driverId}")
    public ResponseEntity<OrderSummaryResponseDto> getOrderSummaryByDriver(@PathVariable Long driverId) {
        OrderSummaryResponseDto summary = orderService.getOrderSummaryByDriver(driverId);
        return ResponseEntity.ok(summary);
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

    @MessageMapping("/orders/orders-details")
    public void handleDriverResponse(requestOrderDistationdetailsDto response) {
        logger.info("Received driver details for order {}",
                response);
        orderService.updateOrderDetails(response.getId(),response.getDistance(),response.getDistanceToShop(),response.getEstimatedTimeToShop());
    }

}
