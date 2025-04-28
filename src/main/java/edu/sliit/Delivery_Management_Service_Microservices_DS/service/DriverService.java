package edu.sliit.Delivery_Management_Service_Microservices_DS.service;

import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.*;

import java.util.List;

public interface DriverService {
    responseDriverDto createDriver(requestDriverDto requestDriverDto);
    responseDriverDto getDriverDetailsByUsername(String username);
    responseDriverDto updateDriverByUsername(String username, updateDriverDto updatedDto);
    List<responseDriverDto> getAllDrivers();
    List<responseDriverAvailableDto> getAvailableDrivers();
    driverAvailableUpdateDto updateDriverAvailable(driverAvailableUpdateDto availableUpdateDto);
    void updateOrderStatusComplted(Long driverId, double lat, double lng);
    usernameDriverResponse getUsernameDrivers(String username);

}
