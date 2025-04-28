package edu.sliit.Delivery_Management_Service_Microservices_DS.service.impl;
import edu.sliit.Delivery_Management_Service_Microservices_DS.controller.DriverController;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.*;
import edu.sliit.Delivery_Management_Service_Microservices_DS.entity.Driver;
import edu.sliit.Delivery_Management_Service_Microservices_DS.repository.DriverRepository;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);


    @Override
    public responseDriverDto createDriver(requestDriverDto requestDriverDto) {
        Driver driver = modelMapper.map(requestDriverDto, Driver.class);
        driver.setAvailable(true);
        Driver savedDriver = driverRepository.save(driver);
        return modelMapper.map(savedDriver, responseDriverDto.class);
    }

    @Override
    public List<responseDriverDto> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(driver -> modelMapper.map(driver, responseDriverDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<responseDriverAvailableDto> getAvailableDrivers() {
        return driverRepository.findAvailableDriversWithIdAndNameOnly().stream()
                .map(driver -> modelMapper.map(driver, responseDriverAvailableDto.class))
                .collect(Collectors.toList());
   }

    @Override
    public usernameDriverResponse getUsernameDrivers(String username) {
        Driver driver = driverRepository.findByUsername(username);
        if (driver == null) {
            return null;
        }
        usernameDriverResponse response = modelMapper.map(driver, usernameDriverResponse.class);
        System.out.println(response.getUsername());
        response.setName(driver.getUsername());
        return response;
    }



    @Override
    public driverAvailableUpdateDto updateDriverAvailable(driverAvailableUpdateDto availableUpdateDto) {
        Driver driver = driverRepository.findById(availableUpdateDto.getId())
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + availableUpdateDto.getId()));

        driver.setAvailable(availableUpdateDto.isAvailable());
        driverRepository.save(driver);
        return availableUpdateDto;
    }

    @Override
    public void updateOrderStatusComplted(Long driverId, double lat, double lng) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        driver.setLatitude((lat));
        driver.setLongitude((lng));
        driverRepository.save(driver);
    }

    @Override
    public responseDriverDto getDriverDetailsByUsername(String username) {
        Driver driver = driverRepository.findByUsername(username);
        if (driver == null) {
            return null;
        }
        return modelMapper.map(driver, responseDriverDto.class);
    }


    @Override
    public responseDriverDto updateDriverByUsername(String username, updateDriverDto updatedDto) {
        Driver driver = driverRepository.findByUsername(username);
        if (driver == null) {
            return null;
        }

        // Update fields
        driver.setName(updatedDto.getName());
        driver.setEmail(updatedDto.getEmail());
        driver.setLatitude(updatedDto.getLatitude());
        driver.setLongitude(updatedDto.getLongitude());
        driver.setLicenseNumber(updatedDto.getLicenseNumber());
        driver.setNic(updatedDto.getNic());
        driver.setVehicleType(updatedDto.getVehicleType());
        driver.setVehicleModel(updatedDto.getVehicleModel());
        driver.setRegistrationNumber(updatedDto.getRegistrationNumber());
        driver.setPhoneNumber(updatedDto.getPhoneNumber());
        driver.setFirstName(updatedDto.getFirstName());
        driver.setLastName(updatedDto.getLastName());
        driver.setVehicleNo(updatedDto.getVehicleNo());
        driver.setLicencePlate(updatedDto.getLicencePlate());
        driver.setLicenceNumber(updatedDto.getLicenceNumber());
        driver.setLicenceExpiryDate(updatedDto.getLicenceExpiryDate());
        driver.setPassword(updatedDto.getPassword());
        driver.setProfileImage(updatedDto.getProfileImage());
        driver.setAddressTestimony(updatedDto.getAddressTestimony());
        driver.setLicenseImagePathFront(updatedDto.getLicenseImagePathFront());
        driver.setLicenseImagePathBack(updatedDto.getLicenseImagePathBack());
        driver.setNicImagePathFront(updatedDto.getNicImagePathFront());
        driver.setNicImagePathBack(updatedDto.getNicImagePathBack());
        driver.setVehicleFrontPath(updatedDto.getVehicleFrontPath());
        driver.setVehicleRearPath(updatedDto.getVehicleRearPath());
        driver.setVehicleSidePath(updatedDto.getVehicleSidePath());
        driver.setVehicleColor(updatedDto.getVehicleColor());
        driver.setVerified(updatedDto.isVerified());

        Driver savedDriver = driverRepository.save(driver);
        return modelMapper.map(savedDriver, responseDriverDto.class);
    }



}
