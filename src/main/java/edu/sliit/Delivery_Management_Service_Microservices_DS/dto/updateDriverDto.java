package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class updateDriverDto {

    @Email(message = "Invalid email format")
    private String email;

    private double latitude;
    private double longitude;

    private String name;

    private String licenseNumber;

    @Pattern(regexp = "^[0-9]{9}[Vv]$|^[0-9]{12}$", message = "NIC must be in correct format")
    private String nic;
    private String vehicleNo;
    private String vehicleType;
    private String vehicleModel;
    private String registrationNumber;
    private String licencePlate;
    private String licenceNumber;
    private String licenceExpiryDate;

    @Pattern(regexp = "^(?:\\+94|0)?7\\d{8}$", message = "Invalid Sri Lankan phone number")
    private String phoneNumber;

    private boolean available;

    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;

    private String firstName;
    private String lastName;

    @Size(min = 6, message = "Password must be at least 6 characters")
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

    private boolean isVerified;
}
