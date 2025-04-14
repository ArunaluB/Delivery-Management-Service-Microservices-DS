package edu.sliit.Delivery_Management_Service_Microservices_DS.repository;

import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {
      @Query(value = "{ 'available': true }", fields = "{ '_id': 1, 'name': 1, 'latitude': 1, 'longitude': 1 }")
      List<Driver> findAvailableDriversWithIdAndNameOnly();
}
