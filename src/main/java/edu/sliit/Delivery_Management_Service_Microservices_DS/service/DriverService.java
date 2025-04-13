package edu.sliit.Delivery_Management_Service_Microservices_DS.service;

import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.driverAvailableUpdateDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.requestDriverDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverAvailableDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverDto;

import java.util.List;

public interface DriverService {
    responseDriverDto createDriver(requestDriverDto requestDriverDto);
    List<responseDriverDto> getAllDrivers();
    List<responseDriverAvailableDto> getAvailableDrivers();
    driverAvailableUpdateDto updateDriverAvailable(driverAvailableUpdateDto availableUpdateDto);
    void updateDriverLocation(String driverId, double lat, double lng);

}
