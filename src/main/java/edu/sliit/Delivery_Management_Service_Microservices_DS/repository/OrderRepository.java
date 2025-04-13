package edu.sliit.Delivery_Management_Service_Microservices_DS.repository;

import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
}
