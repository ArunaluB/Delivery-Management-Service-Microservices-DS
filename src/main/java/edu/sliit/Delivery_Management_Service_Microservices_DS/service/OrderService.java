package edu.sliit.Delivery_Management_Service_Microservices_DS.service;

import edu.sliit.Delivery_Management_Service_Microservices_DS.config.OrderStatus;
import edu.sliit.Delivery_Management_Service_Microservices_DS.entity.Order;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.OrderDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.OrderSummaryResponseDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;

import java.util.List;

public interface OrderService {
    String processOrder(RequestComeOrderDto requestComeOrderDto);
    Order updateOrderStatus(Order order, OrderStatus status);
    void handleDriverResponse(String orderId, Long driverId, boolean accepted);
    void updateOrderStatusComplted(long Id, String status);
    void updateOrderDetails(long Id, double distances,double distanceToShop,double estimatedTimeToShop );
    List<OrderDto> getDeliveredOrRejectedOrdersByDriverId(Long driverId);
    OrderSummaryResponseDto getOrderSummaryByDriver(Long driverId);
};
