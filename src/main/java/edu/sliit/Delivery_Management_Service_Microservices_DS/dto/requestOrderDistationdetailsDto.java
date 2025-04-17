package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class requestOrderDistationdetailsDto {
    private Long id;
    private double distance;
    private double distanceToShop;
    private double estimatedTimeToShop;
}
