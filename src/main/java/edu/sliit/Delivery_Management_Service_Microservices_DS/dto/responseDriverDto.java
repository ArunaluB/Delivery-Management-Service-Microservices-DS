package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class responseDriverDto {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String password;
    private String licenseNumber;
    private String Nic;
    private String vehicleType;
    private String vehicleModel;
    private String registrationNumber;
    private String phoneNumber;
}
