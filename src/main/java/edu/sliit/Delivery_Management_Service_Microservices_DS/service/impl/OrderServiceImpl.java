//package edu.sliit.Delivery_Management_Service_Microservices_DS.service.impl;
//
//import edu.sliit.Delivery_Management_Service_Microservices_DS.config.OrderStatus;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.controller.OrderController;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Order;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverAvailableDto;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.repository.OrderRepository;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.utils.MapboxService;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.slf4j.Logger;
//
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//@Service
//@RequiredArgsConstructor
//public class OrderServiceImpl implements OrderService {
//
//    private final OrderRepository orderRepository;
//    private final DriverService driverService;
//    private final ModelMapper modelMapper;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MapboxService mapboxService;
//    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
//    private static final int DRIVER_RESPONSE_TIMEOUT = 45;
//    private final ExecutorService executorService = Executors.newCachedThreadPool();
//
//    @Override
//    public String processOrder(RequestComeOrderDto requestComeOrderDto) {
//        logger.info("Received new order: {}", requestComeOrderDto);
//        Order order = modelMapper.map(requestComeOrderDto, Order.class);
//
//        // Save the initial order with PENDING status
//        order.setStatus(String.valueOf(OrderStatus.PENDING));
//        order.setCreatedAt(new Date());
//        Order savedOrder = orderRepository.save(order);
//
//        // call assign driver async method
//        CompletableFuture.runAsync(() -> assignDriverToOrder(savedOrder), executorService);
//        return "Order processed successfully";
//    }
//
//
//    private void assignDriverToOrder(Order order) {
//        logger.info("Starting driver assignment for order: {}", order.getId());
//        try {
//            List<responseDriverAvailableDto> availableDrivers = driverService.getAvailableDrivers();
//
//            if (availableDrivers.isEmpty()) {
//                logger.warn("No available drivers found for order: {}", order.getOrderId());
//                updateOrderStatus(order, OrderStatus.NO_DRIVER_AVAILABLE);
//                return;
//            }
//
//            double shopLat = order.getShopLat();
//            double shopLng = order.getShopLng();
//
//            // Calculate and sort drivers by distance to shop
//            Map<responseDriverAvailableDto, Double> driverDistances = new HashMap<>();
//            for (responseDriverAvailableDto driver : availableDrivers) {
//                double distance = mapboxService.calculateDistance(
//                        shopLat,
//                        shopLng,
//                        driver.getLatitude(),
//                        driver.getLongitude()
//                );
//                driverDistances.put(driver, distance);
//                logger.info("Driver {} is {} km from shop", driver.getId(), distance);
//            }
//            // Sort drivers by distance (closest first)
//            ArrayList<responseDriverAvailableDto> sortedDrivers = new ArrayList<>(availableDrivers);
//            sortedDrivers.sort(Comparator.comparing(driverDistances::get));
//
//
//            boolean orderAssigned = false;
//            Set<Long> rejectedDriverIds = new HashSet<>();
//
//            while (!orderAssigned && !sortedDrivers.isEmpty()) {
//                Optional<responseDriverAvailableDto> closestDriverOpt = sortedDrivers.stream()
//                        .filter(d -> !rejectedDriverIds.contains(d.getId()))
//                        .findFirst();
//                if (closestDriverOpt.isEmpty()) break;
//                responseDriverAvailableDto closestDriver = closestDriverOpt.get();
//
//                updateOrderStatus(order, OrderStatus.DRIVER_ASSIGNMENT_IN_PROGRESS);
//
//                String driverDestination = "/queue/driver/" + closestDriver.getId() + "/orders";
//                messagingTemplate.convertAndSend(driverDestination, order);
//                orderAssigned = waitForDriverResponse(order, closestDriver, rejectedDriverIds);
//            }
//
//            if (!orderAssigned) {
//                logger.warn("No driver accepted order: {}", order.getId());
//                updateOrderStatus(order, OrderStatus.NO_DRIVER_ACCEPTED);
//            }
//
//        } catch (Exception e) {
//            logger.error("Error during driver assignment for order: " + order.getId(), e);
//            updateOrderStatus(order, OrderStatus.ERROR);
//        }
//    }
//
//
//    @Override
//    public Order updateOrderStatus(Order order, OrderStatus status) {
//        logger.info("Updating order {} status to {}", order.getId(), status);
//        order.setStatus(String.valueOf(status));
//        Order savedOrder = orderRepository.save(order);
//        return savedOrder;
//    }
//
//    private boolean waitForDriverResponse(Order order, responseDriverAvailableDto driver, Set<Long> rejectedDriverIds) {
//        // Create a CountDownLatch to wait for the driver's response
//        CountDownLatch responseLatch = new CountDownLatch(1);
//        AtomicBoolean accepted = new AtomicBoolean(false);
//
//        // Subscribe to driver's response channel
//        String responseDestination = "/topic/driver/" + driver.getId() + "/response";
//
//        // Set up a temporary subscription to listen for the driver's response
//        // This is simplified; in a real application, you would need to set up a proper message listener
//        // Here we're simulating the WebSocket subscription process
//
//        // Start a timeout task
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//        ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
//            logger.info("Driver {} timed out for order {}", driver.getId(), order.getId());
//            rejectedDriverIds.add(Long.valueOf(driver.getId()));
//            responseLatch.countDown();
//        }, DRIVER_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
//
//        // This would be handled by your WebSocket configuration in a real application
//        // For this example, we'll set up a mock listener that simulates receiving a message
//        messagingTemplate.setUserDestinationPrefix("/topic/driver/" + driver.getId());
//
//        // In a real application, this would be triggered by a message from the driver's client
//        // For now, we'll just simulate a driver response handler
//        CompletableFuture.runAsync(() -> {
//            try {
//                // Wait for actual response from driver
//                // This is where your actual WebSocket subscription would receive a message
//
//                // Assuming we received a message (for demonstration)
//                // In a real application, this would be triggered by an actual WebSocket message
//
//                // This is just for demonstration purposes
//                // In a real application, wait for the actual driver response
//
//                // If the driver accepted
//                if (Math.random() > 0.5) { // Simulate acceptance (50% chance)
//                    accepted.set(true);
//                    updateOrderStatus(order, OrderStatus.ACCEPTED);
//                    order.setDriverId(driver.getId());
//                } else {
//                    // Driver rejected
//                    rejectedDriverIds.add(Long.valueOf(driver.getId()));
//                }
//
//                // Cancel the timeout task since we got a response
//                timeoutTask.cancel(false);
//                responseLatch.countDown();
//
//            } catch (Exception e) {
//                logger.error("Error processing driver response", e);
//                timeoutTask.cancel(false);
//                responseLatch.countDown();
//            }
//        });
//
//        try {
//            // Wait for either a response or timeout
//            responseLatch.await();
//        } catch (InterruptedException e) {
//            logger.error("Interrupted while waiting for driver response", e);
//            Thread.currentThread().interrupt();
//        } finally {
//            scheduler.shutdown();
//        }
//
//        return accepted.get();
//    }
//}
//
//package edu.sliit.Delivery_Management_Service_Microservices_DS.service.impl;
//
//import edu.sliit.Delivery_Management_Service_Microservices_DS.config.OrderStatus;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.controller.OrderController;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Order;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.DriverResponseDto;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverAvailableDto;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.repository.OrderRepository;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
//import edu.sliit.Delivery_Management_Service_Microservices_DS.utils.MapboxService;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.slf4j.LoggerFactory;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.slf4j.Logger;
//import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//
//import java.util.*;
//import java.util.concurrent.*;
//
//@Service
//@RequiredArgsConstructor
//public class OrderServiceImpl implements OrderService {
//
//    private final OrderRepository orderRepository;
//    private final DriverService driverService;
//    private final ModelMapper modelMapper;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final MapboxService mapboxService;
//    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
//    private static final int DRIVER_RESPONSE_TIMEOUT = 45;
//    private final ExecutorService executorService = Executors.newCachedThreadPool();
//
//    // Map to track pending driver confirmations
//    private final Map<String, CompletableFuture<Boolean>> driverResponses = new ConcurrentHashMap<>();
//
//    @Override
//    public String processOrder(RequestComeOrderDto requestComeOrderDto) {
//        logger.info("Received new order: {}", requestComeOrderDto);
//        Order order = modelMapper.map(requestComeOrderDto, Order.class);
//
//        // Save the initial order with PENDING status
//        order.setStatus(String.valueOf(OrderStatus.PENDING));
//        order.setCreatedAt(new Date());
//        Order savedOrder = orderRepository.save(order);
//
//        // call assign driver async method
//        CompletableFuture.runAsync(() -> assignDriverToOrder(savedOrder), executorService);
//        return "Order processed successfully";
//    }
//
//    private void assignDriverToOrder(Order order) {
//        logger.info("Starting driver assignment for order: {}", order.getId());
//        try {
//            List<responseDriverAvailableDto> availableDrivers = driverService.getAvailableDrivers();
//
//            if (availableDrivers.isEmpty()) {
//                logger.warn("No available drivers found for order: {}", order.getOrderId());
//                updateOrderStatus(order, OrderStatus.NO_DRIVER_AVAILABLE);
//                return;
//            }
//
//            double shopLat = order.getShopLat();
//            double shopLng = order.getShopLng();
//
//            // Calculate and sort drivers by distance to shop
//            Map<responseDriverAvailableDto, Double> driverDistances = new HashMap<>();
//            for (responseDriverAvailableDto driver : availableDrivers) {
//                double distance = mapboxService.calculateDistance(
//                        shopLat,
//                        shopLng,
//                        driver.getLatitude(),
//                        driver.getLongitude()
//                );
//                driverDistances.put(driver, distance);
//                logger.info("Driver {} is {} km from shop", driver.getId(), distance);
//            }
//            logger.info("Driver assigned distances: {}", driverDistances);
//            // Sort drivers by distance (closest first)
//            List<responseDriverAvailableDto> sortedDrivers = new ArrayList<>(availableDrivers);
//            sortedDrivers.sort(Comparator.comparing(driverDistances::get));
//
//            boolean orderAssigned = false;
//            Set<String> rejectedDriverIds = new HashSet<>();
//
//            for (responseDriverAvailableDto driver : sortedDrivers) {
//                if (rejectedDriverIds.contains(driver.getId())) {
//                    continue;
//                }
//
//                updateOrderStatus(order, OrderStatus.DRIVER_ASSIGNMENT_IN_PROGRESS);
//
//                // Unique key for tracking this specific order-driver assignment attempt
//                String responseKey = "order-" + order.getId() + "-driver-" + driver.getId();
//                CompletableFuture<Boolean> responseFuture = new CompletableFuture<>();
//                driverResponses.put(responseKey, responseFuture);
//
//                // Send order to driver
//                String driverDestination = "/queue/driver/" + driver.getId() + "/orders";
//                logger.info("Sending order {} to driver {}", order.getId(), driver.getId());
//                messagingTemplate.convertAndSend(driverDestination, order);
//
//                try {
//                    // Wait for driver response with timeout
//                    Boolean accepted = responseFuture.get(DRIVER_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
//
//                    if (accepted) {
//                        // Driver accepted the order
//                        order.setDriverId(driver.getId());
//                        updateOrderStatus(order, OrderStatus.ACCEPTED);
//                        orderAssigned = true;
//                        logger.info("Driver {} accepted order {}", driver.getId(), order.getId());
//                        break;
//                    } else {
//                        // Driver explicitly rejected
//                        rejectedDriverIds.add(driver.getId());
//                        logger.info("Driver {} rejected order {}", driver.getId(), order.getId());
//                    }
//                } catch (TimeoutException e) {
//                    // Driver didn't respond in time
//                    rejectedDriverIds.add(driver.getId());
//                    logger.info("Driver {} timed out for order {}", driver.getId(), order.getId());
//                } finally {
//                    // Clean up
//                    driverResponses.remove(responseKey);
//                }
//            }
//
//            if (!orderAssigned) {
//                logger.warn("No driver accepted order: {}", order.getId());
//                updateOrderStatus(order, OrderStatus.NO_DRIVER_ACCEPTED);
//            }
//
//        } catch (Exception e) {
//            logger.error("Error during driver assignment for order: " + order.getId(), e);
//            updateOrderStatus(order, OrderStatus.ERROR);
//        }
//    }
//
//    @Override
//    public Order updateOrderStatus(Order order, OrderStatus status) {
//        logger.info("Updating order {} status to {}", order.getId(), status);
//        order.setStatus(String.valueOf(status));
//        Order savedOrder = orderRepository.save(order);
//
//        // Notify about order status change
//        messagingTemplate.convertAndSend("/topic/orders/" + order.getId() + "/status", status);
//
//        return savedOrder;
//    }
//
//    /**
//     * Handle driver responses to order assignments
//     */
//    @MessageMapping("/driver/response")
//    public void handleDriverResponse(@Payload DriverResponseDto response) {
//        logger.info("Received response from driver {}: {}", response.getDriverId(), response);
//
//        String responseKey = "order-" + response.getOrderId() + "-driver-" + response.getDriverId();
//        CompletableFuture<Boolean> responseFuture = driverResponses.get(responseKey);
//
//        if (responseFuture != null) {
//            responseFuture.complete(response.isAccepted());
//        } else {
//            logger.warn("Received driver response for unknown order-driver combination: {}", responseKey);
//        }
//    }
//}


package edu.sliit.Delivery_Management_Service_Microservices_DS.service.impl;

import edu.sliit.Delivery_Management_Service_Microservices_DS.config.OrderStatus;
import edu.sliit.Delivery_Management_Service_Microservices_DS.controller.OrderController;
import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Order;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.DriverResponseDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverAvailableDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.repository.OrderRepository;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
import edu.sliit.Delivery_Management_Service_Microservices_DS.utils.MapboxService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DriverService driverService;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapboxService mapboxService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final int DRIVER_RESPONSE_TIMEOUT = 45;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // Map to track pending driver confirmations
    private final Map<String, CompletableFuture<Boolean>> driverResponses = new ConcurrentHashMap<>();

    @PostConstruct
    public void setupModelMapper() {
        modelMapper.createTypeMap(RequestComeOrderDto.class, Order.class)
                .addMappings(mapper -> {
                    // Skip the id field mapping
                    mapper.skip(Order::setId);
                    // Ensure orderId maps correctly
                    mapper.map(RequestComeOrderDto::getOrderId, Order::setOrderId);
                });
    }

    @Override
    public String processOrder(RequestComeOrderDto requestComeOrderDto) {
        logger.info("Received new order: {}", requestComeOrderDto);
        Order order = modelMapper.map(requestComeOrderDto, Order.class);

        // Save the initial order with PENDING status
        order.setStatus(String.valueOf(OrderStatus.PENDING));
        order.setCreatedAt(new Date());
        Order savedOrder = orderRepository.save(order);

        // call assign driver async method
        CompletableFuture.runAsync(() -> assignDriverToOrder(savedOrder), executorService);
        return "Order processed successfully";
    }

    private void assignDriverToOrder(Order order) {
        logger.info("Starting driver assignment for order: {}", order.getId());
        try {
            List<responseDriverAvailableDto> availableDrivers = driverService.getAvailableDrivers();
            logger.info("Drivers available: {}", availableDrivers);
            if (availableDrivers.isEmpty()) {
                logger.warn("No available drivers found for order: {}", order.getOrderId());
                updateOrderStatus(order, OrderStatus.NO_DRIVER_AVAILABLE);
                return;
            }

            double shopLat = order.getShopLat();
            double shopLng = order.getShopLng();
            logger.info("Shop lat: {} , lng: {}", shopLat, shopLng);
            logger.info("Driver lat: {} , lng: {}", shopLat, shopLng);

            // Filter drivers to include only those within max distance range
            List<DriverDistanceInfo> nearbyDrivers = new ArrayList<>();

            for (responseDriverAvailableDto driver : availableDrivers) {
                double distance = mapboxService.calculateDistance(
                        shopLat,
                        shopLng,
                        driver.getLatitude(),
                        driver.getLongitude()
                );
                logger.info("Driver lat: {} , lng: {}", driver.getLatitude(), shopLng);
                // Get estimated travel time if driver is within distance range
                double estimatedMinutes = -1;
                if (distance <= mapboxService.getMaxDriverDistance()) {
                    estimatedMinutes = mapboxService.getEstimatedTravelTime(
                            driver.getLatitude(),
                            driver.getLongitude(),
                            shopLat,
                            shopLng
                    );

                    nearbyDrivers.add(new DriverDistanceInfo(driver, distance, estimatedMinutes));

                    logger.info("Driver {} is {} km from shop - within range (ETA: {} minutes)",
                            driver.getId(), distance, estimatedMinutes);
                } else {
                    logger.info("Driver {} is {} km from shop - outside of range (max: {}km)",
                            driver.getId(), distance, mapboxService.getMaxDriverDistance());
                }
            }

            if (nearbyDrivers.isEmpty()) {
                logger.warn("No nearby drivers found for order: {}", order.getOrderId());
                updateOrderStatus(order, OrderStatus.NO_DRIVER_AVAILABLE);
                return;
            }

            // Sort nearby drivers by distance (closest first)
            nearbyDrivers.sort(Comparator.comparing(DriverDistanceInfo::getDistance));

            // Log nearby drivers for debugging
            logger.info("Found {} nearby drivers for order {}", nearbyDrivers.size(), order.getId());
            for (int i = 0; i < nearbyDrivers.size(); i++) {
                DriverDistanceInfo driverInfo = nearbyDrivers.get(i);
                logger.info("Nearby driver #{}: ID={}, distance={}km, ETA={} minutes",
                        i+1, driverInfo.getDriver().getId(),
                        driverInfo.getDistance(), driverInfo.getEstimatedMinutes());
            }

            boolean orderAssigned = false;
            Set<Long> rejectedDriverIds = new HashSet<>();

            for (DriverDistanceInfo driverInfo : nearbyDrivers) {
                responseDriverAvailableDto driver = driverInfo.getDriver();

                if (rejectedDriverIds.contains(driver.getId())) {
                    continue;
                }

                updateOrderStatus(order, OrderStatus.DRIVER_ASSIGNMENT_IN_PROGRESS);

                // Unique key for tracking this specific order-driver assignment attempt
                String responseKey = "order-" + order.getId() + "-driver-" + driver.getId();
                CompletableFuture<Boolean> responseFuture = new CompletableFuture<>();
                driverResponses.put(responseKey, responseFuture);

                // Add distance and ETA information to the order object for driver
                Map<String, Object> orderWithDistanceInfo = new HashMap<>();
                orderWithDistanceInfo.put("order", order);
                orderWithDistanceInfo.put("distanceKm", driverInfo.getDistance());
                orderWithDistanceInfo.put("estimatedMinutes", driverInfo.getEstimatedMinutes());

                // Send order to driver
                String driverDestination = "/queue/driver/" + driver.getId() + "/orders";
                logger.info("Sending order {} to driver {} (distance: {}km, ETA: {} min)",
                        order.getId(), driver.getId(),
                        driverInfo.getDistance(), driverInfo.getEstimatedMinutes());
                messagingTemplate.convertAndSend(driverDestination, orderWithDistanceInfo);

                try {
                    // Wait for driver response with timeout
                    Boolean accepted = responseFuture.get(DRIVER_RESPONSE_TIMEOUT, TimeUnit.SECONDS);

                    if (accepted) {
                        // Driver accepted the order
                        order.setDriverId(driver.getId());
                        // Store the distance information in the order
                        order.setDistanceToShop(driverInfo.getDistance());
                        order.setEstimatedTimeToShop(driverInfo.getEstimatedMinutes());

                        updateOrderStatus(order, OrderStatus.ACCEPTED);
                        orderAssigned = true;
                        logger.info("Driver {} accepted order {}", driver.getId(), order.getId());
                        break;
                    } else {
                        // Driver explicitly rejected
                        rejectedDriverIds.add(driver.getId());
                        logger.info("Driver {} rejected order {}", driver.getId(), order.getId());
                    }
                } catch (TimeoutException e) {
                    // Driver didn't respond in time
                    rejectedDriverIds.add(driver.getId());
                    logger.info("Driver {} timed out for order {}", driver.getId(), order.getId());
                } finally {
                    // Clean up
                    driverResponses.remove(responseKey);
                }
            }

            if (!orderAssigned) {
                logger.warn("No driver accepted order: {}", order.getId());
                updateOrderStatus(order, OrderStatus.NO_DRIVER_ACCEPTED);
            }

        } catch (Exception e) {
            logger.error("Error during driver assignment for order: " + order.getId(), e);
            updateOrderStatus(order, OrderStatus.ERROR);
        }
    }

    @Override
    public Order updateOrderStatus(Order order, OrderStatus status) {
        logger.info("Updating order {} status to {}", order.getId(), status);
        order.setStatus(String.valueOf(status));
        Order savedOrder = orderRepository.save(order);

        // Notify about order status change
        messagingTemplate.convertAndSend("/topic/orders/" + order.getId() + "/status", status);

        return savedOrder;
    }

    /**
     * Handle response from driver about accepting/rejecting an order
     */
    @Override
    public void handleDriverResponse(String orderId, Long driverId, boolean accepted) {
        // Create the response key that matches what's used in assignDriverToOrder
        String responseKey = "order-" + orderId + "-driver-" + driverId;

        CompletableFuture<Boolean> responseFuture = driverResponses.get(responseKey);
        if (responseFuture != null) {
            // Complete the future with the driver's response
            responseFuture.complete(accepted);
            logger.info("Completed response future for key: {}", responseKey);
        } else {
            logger.warn("No pending response future found for key: {}", responseKey);
        }
    }

    /**
     * Handle driver responses to order assignments
     */
    @MessageMapping("/driver/response")
    public void handleDriverResponse(@Payload DriverResponseDto response) {
        logger.info("Received response from driver {}: {}", response.getDriverId(), response);

        String responseKey = "order-" + response.getOrderId() + "-driver-" + response.getDriverId();
        CompletableFuture<Boolean> responseFuture = driverResponses.get(responseKey);

        if (responseFuture != null) {
            responseFuture.complete(response.isAccepted());
        } else {
            logger.warn("Received driver response for unknown order-driver combination: {}", responseKey);
        }
    }

    /**
     * Helper class to store driver distance information
     */
    private static class DriverDistanceInfo {
        private final responseDriverAvailableDto driver;
        private final double distance;
        private final double estimatedMinutes;

        public DriverDistanceInfo(responseDriverAvailableDto driver, double distance, double estimatedMinutes) {
            this.driver = driver;
            this.distance = distance;
            this.estimatedMinutes = estimatedMinutes;
        }

        public responseDriverAvailableDto getDriver() {
            return driver;
        }

        public double getDistance() {
            return distance;
        }

        public double getEstimatedMinutes() {
            return estimatedMinutes;
        }
    }

}