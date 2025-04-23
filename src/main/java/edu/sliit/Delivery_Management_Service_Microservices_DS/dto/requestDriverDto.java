package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class requestDriverDto {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private double latitude;
    private double longitude;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "NIC is required")
    @Pattern(regexp = "^[0-9]{9}[Vv]$|^[0-9]{12}$", message = "NIC must be in correct format")
    private String nic;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(?:\\+94|0)?7\\d{8}$", message = "Invalid Sri Lankan phone number")
    private String phoneNumber;

    private boolean available;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;

    private String firstName;
    private String lastName;

    @NotBlank(message = "Password is required")
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
