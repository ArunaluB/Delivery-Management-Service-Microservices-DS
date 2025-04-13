package edu.sliit.Delivery_Management_Service_Microservices_DS.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "driver")
public class Driver {
    @Id
    private String id;
    private String name;
    private String email;
    private double latitude;
    private double longitude;
    private String licenseNumber;
    private String Nic;
    private String vehicleType;
    private String vehicleModel;
    private String registrationNumber;
    private String phoneNumber;
    private boolean available;
}
