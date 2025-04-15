package edu.sliit.Delivery_Management_Service_Microservices_DS.repository;

import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);

}
