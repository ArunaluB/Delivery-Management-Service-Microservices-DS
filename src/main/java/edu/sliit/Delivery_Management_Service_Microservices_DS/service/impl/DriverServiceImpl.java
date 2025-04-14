package edu.sliit.Delivery_Management_Service_Microservices_DS.service.impl;
import edu.sliit.Delivery_Management_Service_Microservices_DS.controller.DriverController;
import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Driver;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.driverAvailableUpdateDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.requestDriverDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverAvailableDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverDto;
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
    public driverAvailableUpdateDto updateDriverAvailable(driverAvailableUpdateDto availableUpdateDto) {
        Driver driver = driverRepository.findById(availableUpdateDto.getId())
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + availableUpdateDto.getId()));

        driver.setAvailable(availableUpdateDto.isAvailable());
        driverRepository.save(driver);
        return availableUpdateDto;
    }

    @Override
    public void updateDriverLocation(Long driverId, double lat, double lng) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));

        driver.setLatitude((lat));
        driver.setLongitude((lng));
        driverRepository.save(driver);
    }

}
