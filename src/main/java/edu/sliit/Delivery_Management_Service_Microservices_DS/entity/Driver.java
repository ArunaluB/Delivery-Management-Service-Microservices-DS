package edu.sliit.Delivery_Management_Service_Microservices_DS.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "driver")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private double latitude;
    private double longitude;
    private String licenseNumber;
    private String nic;
    private String vehicleType;
    private String vehicleModel;
    private String registrationNumber;
    private String phoneNumber;
    private boolean available;
}
