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
    private double latitude;
    private double longitude;
    private String licenseNumber;
    private String nic;
    private String vehicleType;
    private String vehicleModel;
    private String registrationNumber;
    private String phoneNumber;
    private boolean available;
    private String username;
    private String firstName;
    private String lastName;
    private String vehicleNo;
    private String licencePlate;
    private String licenceNumber;
    private String licenceExpiryDate;
    private String password;
    private String profileImage;
    private String addressTestimony;
    private String licenseImagePathFront;
    private String licenseImagePathBack;
    private String nicImagePathFront;
    private String nicImagePathBack;
    private String vehicleFrontPath;
    private String vehicleRearPath;
    private String vehicleSidePath;
    private String vehicleColor;
    private boolean isVerified = false;

}
