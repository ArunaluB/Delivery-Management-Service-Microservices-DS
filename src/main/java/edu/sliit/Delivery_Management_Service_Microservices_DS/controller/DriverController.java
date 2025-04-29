package edu.sliit.Delivery_Management_Service_Microservices_DS.controller;

import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.*;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<responseDriverDto> createDriver(@RequestBody requestDriverDto requestDriverDto) {
        responseDriverDto createdDriver = driverService.createDriver(requestDriverDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdDriver);
    }

    @GetMapping
    public ResponseEntity<List<responseDriverDto>> getAllDrivers() {
        List<responseDriverDto> drivers = driverService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }

    @GetMapping("/available")
    public ResponseEntity<List<responseDriverAvailableDto>> getAvailableDrivers() {
        List<responseDriverAvailableDto> availableDrivers = driverService.getAvailableDrivers();
        return ResponseEntity.ok(availableDrivers);
    }

    @GetMapping("/username")
    public ResponseEntity<usernameDriverResponse> getUsernameDrivers(@RequestBody String username) {
        usernameDriverResponse availableDrivers = driverService.getUsernameDrivers(username);
        return ResponseEntity.ok(availableDrivers);
    }

    @PutMapping("/available")
    // @MessageMapping("/driver/driver-available")
    public ResponseEntity<driverAvailableUpdateDto> updateDriverAvailable(@RequestBody driverAvailableUpdateDto availableUpdateDto) {
        driverAvailableUpdateDto updated = driverService.updateDriverAvailable(availableUpdateDto);
        return ResponseEntity.ok(updated);
    }

}
