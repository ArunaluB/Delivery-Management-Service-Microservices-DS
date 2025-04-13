package edu.sliit.Delivery_Management_Service_Microservices_DS.controller;

import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> processOrder(@RequestBody RequestComeOrderDto order) {
        String result = orderService.processOrder(order);
        return ResponseEntity.ok(result);
    }



}
