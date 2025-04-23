package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class usernameDriverResponse {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String username;
    private String profileImage;
    private boolean isVerified = false;

}
