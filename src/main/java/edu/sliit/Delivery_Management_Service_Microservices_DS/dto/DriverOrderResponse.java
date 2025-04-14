package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DriverOrderResponse {
    private String orderId;
    private Long driverId;
    private boolean accepted;
}
