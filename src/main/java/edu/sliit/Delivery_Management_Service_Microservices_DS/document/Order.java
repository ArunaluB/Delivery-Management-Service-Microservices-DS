package edu.sliit.Delivery_Management_Service_Microservices_DS.document;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "customer_order")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "item_name")
    @Column(name = "quantity")
    private Map<String, Integer> items;

    private String status;
    private double distanceToShop;
    private double estimatedTimeToShop;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
