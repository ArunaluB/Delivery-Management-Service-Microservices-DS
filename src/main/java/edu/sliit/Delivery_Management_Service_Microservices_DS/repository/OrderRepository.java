package edu.sliit.Delivery_Management_Service_Microservices_DS.repository;

import edu.sliit.Delivery_Management_Service_Microservices_DS.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByDriverIdAndStatusIn(Long driverId, List<String> status);
}
