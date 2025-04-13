package edu.sliit.Delivery_Management_Service_Microservices_DS.service.impl;

import edu.sliit.Delivery_Management_Service_Microservices_DS.config.OrderStatus;
import edu.sliit.Delivery_Management_Service_Microservices_DS.controller.OrderController;
import edu.sliit.Delivery_Management_Service_Microservices_DS.document.Order;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.RequestComeOrderDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.responseDriverAvailableDto;
import edu.sliit.Delivery_Management_Service_Microservices_DS.repository.OrderRepository;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
import edu.sliit.Delivery_Management_Service_Microservices_DS.utils.MapboxService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DriverService driverService;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final  MapboxService mapboxService;
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final int DRIVER_RESPONSE_TIMEOUT = 45;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

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

            if (availableDrivers.isEmpty()) {
                logger.warn("No available drivers found for order: {}", order.getOrderId());
                updateOrderStatus(order, OrderStatus.NO_DRIVER_AVAILABLE);
                return;
            }

            double shopLat = order.getShopLat();
            double shopLng = order.getShopLng();

            // Calculate and sort drivers by distance to shop
            Map<responseDriverAvailableDto, Double> driverDistances = new HashMap<>();
            for (responseDriverAvailableDto driver : availableDrivers) {
                double distance = mapboxService.calculateDistance(
                        shopLat,
                        shopLng,
                        driver.getLatitude(),
                        driver.getLongitude()
                );
                driverDistances.put(driver, distance);
                logger.info("Driver {} is {} km from shop", driver.getId(), distance);
            }
            // Sort drivers by distance (closest first)
            ArrayList<responseDriverAvailableDto> sortedDrivers = new ArrayList<>(availableDrivers);
            sortedDrivers.sort(Comparator.comparing(driverDistances::get));


            boolean orderAssigned = false;
            Set<Long> rejectedDriverIds = new HashSet<>();

            while (!orderAssigned && !sortedDrivers.isEmpty()) {
                Optional<responseDriverAvailableDto> closestDriverOpt = sortedDrivers.stream()
                        .filter(d -> !rejectedDriverIds.contains(d.getId()))
                        .findFirst();
                if (closestDriverOpt.isEmpty()) break;
                responseDriverAvailableDto closestDriver = closestDriverOpt.get();

                updateOrderStatus(order, OrderStatus.DRIVER_ASSIGNMENT_IN_PROGRESS);

                String driverDestination = "/queue/driver/" + closestDriver.getId() + "/orders";
                messagingTemplate.convertAndSend(driverDestination, order);
                orderAssigned = waitForDriverResponse(order, closestDriver, rejectedDriverIds);
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
        return savedOrder;
    }

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
private boolean waitForDriverResponse(Order order, responseDriverAvailableDto driver, Set<Long> rejectedDriverIds) {
    // Create a CountDownLatch to wait for the driver's response
    CountDownLatch responseLatch = new CountDownLatch(1);
    AtomicBoolean accepted = new AtomicBoolean(false);

    // Subscribe to driver's response channel
    String responseDestination = "/topic/driver/" + driver.getId() + "/response";

    // Set up a temporary subscription to listen for the driver's response
    // This is simplified; in a real application, you would need to set up a proper message listener
    // Here we're simulating the WebSocket subscription process

    // Start a timeout task
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
        logger.info("Driver {} timed out for order {}", driver.getId(), order.getId());
        rejectedDriverIds.add(Long.valueOf(driver.getId()));
        responseLatch.countDown();
    }, DRIVER_RESPONSE_TIMEOUT, TimeUnit.SECONDS);

    // This would be handled by your WebSocket configuration in a real application
    // For this example, we'll set up a mock listener that simulates receiving a message
    messagingTemplate.setUserDestinationPrefix("/topic/driver/" + driver.getId());

    // In a real application, this would be triggered by a message from the driver's client
    // For now, we'll just simulate a driver response handler
    CompletableFuture.runAsync(() -> {
        try {
            // Wait for actual response from driver
            // This is where your actual WebSocket subscription would receive a message

            // Assuming we received a message (for demonstration)
            // In a real application, this would be triggered by an actual WebSocket message

            // This is just for demonstration purposes
            // In a real application, wait for the actual driver response

            // If the driver accepted
            if (Math.random() > 0.5) { // Simulate acceptance (50% chance)
                accepted.set(true);
                updateOrderStatus(order, OrderStatus.ACCEPTED);
                order.setDriverId(driver.getId());
                orderRepository.save(order);
            } else {
                // Driver rejected
                rejectedDriverIds.add(Long.valueOf(driver.getId()));
            }

            // Cancel the timeout task since we got a response
            timeoutTask.cancel(false);
            responseLatch.countDown();

        } catch (Exception e) {
            logger.error("Error processing driver response", e);
            timeoutTask.cancel(false);
            responseLatch.countDown();
        }
    });

    try {
        // Wait for either a response or timeout
        responseLatch.await();
    } catch (InterruptedException e) {
        logger.error("Interrupted while waiting for driver response", e);
        Thread.currentThread().interrupt();
    } finally {
        scheduler.shutdown();
    }

    return accepted.get();
}
}
