package edu.sliit.Delivery_Management_Service_Microservices_DS.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customer_order")
public class Order {
    @Id
    private String id;
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
}
