package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponseDto {
    private int totalOrders;
    private double totalEarnings;
    private double totalDistance;
    private Map<Integer, DailySummaryDto> dailyTrips;
    private Map<Integer, MonthlySummaryDto> monthlyTrips;
}
