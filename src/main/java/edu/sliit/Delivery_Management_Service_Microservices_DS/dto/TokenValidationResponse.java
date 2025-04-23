package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TokenValidationResponse {
    private boolean valid;
    private String username;
    private String role;
}
