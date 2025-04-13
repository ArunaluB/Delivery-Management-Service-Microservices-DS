package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestComeOrderDto {

    private String orderId;
    private String customerName;
    private String customerPhone;
    private String shop;
    private double shopLat;
    private double shopLng;
    private String customerAddress;
    private double customerLat;
    private double customerLng;
    private double amount;
    private Map<String, Integer> items;
}
