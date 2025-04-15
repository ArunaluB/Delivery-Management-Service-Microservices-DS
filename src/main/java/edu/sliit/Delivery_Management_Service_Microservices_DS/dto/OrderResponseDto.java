package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderResponseDto {
   private Long id;
   private String status;
}
