package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class responseDriverAvailableDto {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
}
