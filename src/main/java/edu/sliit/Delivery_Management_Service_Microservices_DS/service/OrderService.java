package edu.sliit.Delivery_Management_Service_Microservices_DS.service;

import edu.sliit.Delivery_Management_Service_Microservices_DS.config.OrderStatus;
import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Order;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;

public interface OrderService {
    String processOrder(RequestComeOrderDto requestComeOrderDto);
    Order updateOrderStatus(Order order, OrderStatus status);
    void handleDriverResponse(String orderId, Long driverId, boolean accepted);
    void updateOrderStatusComplted(long Id, String status);
};
