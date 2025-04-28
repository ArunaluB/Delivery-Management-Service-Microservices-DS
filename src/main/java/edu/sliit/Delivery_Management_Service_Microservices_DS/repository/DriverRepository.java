package edu.sliit.Delivery_Management_Service_Microservices_DS.repository;

import edu.sliit.Delivery_Management_Service_Microservices_DS.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

      @Query("SELECT d FROM Driver d WHERE d.available = true")
      List<Driver> findAvailableDriversWithIdAndNameOnly();

      Driver findByUsername(String username);

}

