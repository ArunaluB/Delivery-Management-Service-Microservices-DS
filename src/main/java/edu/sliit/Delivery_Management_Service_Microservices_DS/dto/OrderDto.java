package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private String orderId;
    private Long driverId;
    private String customerName;
    private String customerPhone;
    private String shop;
    private double shopLat;
    private double shopLng;
    private String customerAddress;
    private double customerLat;
    private double customerLng;
    private double amount;
    private double distance;
    private Map<String, Integer> items;
    private String status;
    private double distanceToShop;
    private double estimatedTimeToShop;
    private Date createdAt;
}