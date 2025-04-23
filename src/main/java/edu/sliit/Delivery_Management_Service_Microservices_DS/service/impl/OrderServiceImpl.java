package edu.sliit.Delivery_Management_Service_Microservices_DS.service.impl;

import edu.sliit.Delivery_Management_Service_Microservices_DS.config.OrderStatus;
import edu.sliit.Delivery_Management_Service_Microservices_DS.controller.OrderController;
import edu.sliit.Delivery_Management_Service_Microservices_DS.entity.Order;
import edu.sliit.Delivery_Management_Service_Microservices_DS.dto.*;
import edu.sliit.Delivery_Management_Service_Microservices_DS.repository.OrderRepository;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.DriverService;
import edu.sliit.Delivery_Management_Service_Microservices_DS.service.OrderService;
import edu.sliit.Delivery_Management_Service_Microservices_DS.utils.MapboxService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DriverService driverService;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapboxService mapboxService;
    private final KafkaTemplate<String, OrderStatusUpdateEvent> kafkaTemplate; // Added KafkaTemplate
    private static final String ORDER_STATUS_TOPIC = "order-status-updates"; // Kafka topic for order status updates

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

//    @Override
//    public void updateOrderStatusComplted(long Id, String status) {
//        logger.info("Updating order {} status to {}", Id, status);
//        Order order = orderRepository.findById(Id)
//                .orElseThrow(() -> new RuntimeException("Order not found with id: " + Id));
//        logger.info("Updating order {} status to {}", Id, status);
//        order.setStatus(status);
//        orderRepository.save(order);
//        if ("DELIVERED".equalsIgnoreCase(status)) {
//            // Create Kafka event
//            logger.debug("Creating OrderStatusUpdateEvent for orderId={}", Id);
//            OrderStatusUpdateEvent event = new OrderStatusUpdateEvent(
//                    order.getId(),
//                    order.getOrderId(),
//                    "COMPLETED",
//                    order.getDriverId(),
//                    new Date()
//            );
//            logger.info("OrderStatusUpdateEvent created: orderId={}, orderRefId={}, status={}, driverId={}",
//                    event.getOrderId(), event.getOrderRefId(), event.getStatus(), event.getDriverId());
//
//            // Send Kafka message
//            logger.debug("Sending Kafka message to topic={}: orderId={}", ORDER_STATUS_TOPIC, Id);
//            try {
//                CompletableFuture<SendResult<String, OrderStatusUpdateEvent>> future =
//                        kafkaTemplate.send(ORDER_STATUS_TOPIC, String.valueOf(order.getId()), event);
//
//                future.whenComplete((result, ex) -> {
//                    if (ex == null) {
//                        logger.info("Successfully published Kafka event for order: orderId={}, topic={}, partition={}, offset={}",
//                                Id, ORDER_STATUS_TOPIC, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
//                    } else {
//                        logger.error("Failed to publish Kafka event for order: orderId={}, topic={}, error={}",
//                                Id, ORDER_STATUS_TOPIC, ex.getMessage(), ex);
//                        throw new RuntimeException("Kafka send failed for order " + Id, ex);
//                    }
//                });
//            } catch (Exception e) {
//                logger.error("Exception while sending Kafka event for order: orderId={}, error={}", Id, e.getMessage(), e);
//                throw e; // Trigger retry
//            }
//        } else {
//            logger.warn("Status update for order {} ignored; only 'COMPLETED' status triggers Kafka event", Id);
//        }
//        logger.info("Order {} status updated to {} by driver {}", Id, status);
//    }

    @Override
    public void updateOrderStatusComplted(long Id, String status) {
        logger.info("Updating order {} status to {}", Id, status);
        Order order = orderRepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + Id));

        order.setStatus(status);
        orderRepository.save(order);

        if ("DELIVERED".equalsIgnoreCase(status)) {
            try {
                OrderStatusUpdateEvent event = new OrderStatusUpdateEvent(
                        order.getId(),
                        order.getOrderId(),
                        "COMPLETED",
                        order.getDriverId(),
                        new Date()
                );

                logger.info("Creating OrderStatusUpdateEvent for orderId={}", Id);
                logger.info("Event details: orderId={}, orderRefId={}, status={}, driverId={}",
                        event.getOrderId(), event.getOrderRefId(), event.getStatus(), event.getDriverId());

                // Send Kafka message
                CompletableFuture<SendResult<String, OrderStatusUpdateEvent>> future =
                        kafkaTemplate.send(ORDER_STATUS_TOPIC, String.valueOf(order.getId()), event);

                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Successfully published Kafka event for order: orderId={}, topic={}, partition={}, offset={}",
                                Id, ORDER_STATUS_TOPIC, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    } else {
                        logger.error("Failed to publish Kafka event for order: orderId={}, topic={}, error={}",
                                Id, ORDER_STATUS_TOPIC, ex.getMessage(), ex);
                    }
                });

                logger.info("Order {} status updated to COMPLETED and Kafka event triggered", Id);
            } catch (Exception e) {
                logger.error("Exception while sending Kafka event for order: orderId={}, error={}", Id, e.getMessage(), e);
                throw e; // Re-throw to enable retry if needed
            }
        } else {
            logger.info("Status update for order {} to {} (no Kafka event triggered)", Id, status);
        }
    }

    @Override
    @Transactional
    public void updateOrderDetails(long Id, double distances, double distanceToShop, double estimatedTimeToShop) {
        logger.info("Updating order updateorder details {} details to {}", Id, distances);
        Order order = orderRepository.findById(Id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + Id));
        logger.info("Updating order {} status to {}", Id,distanceToShop, estimatedTimeToShop);
        order.setDistance(distances);
        order.setDistanceToShop(distanceToShop);
        order.setEstimatedTimeToShop(estimatedTimeToShop);
        orderRepository.save(order);
        logger.info("Order {} details updated to {}", Id, distances);
    }

    @Override
    public List<OrderDto> getDeliveredOrRejectedOrdersByDriverId(Long driverId) {
        List<Order> orders = orderRepository.findByDriverIdAndStatusIn(
                driverId, List.of("DELIVERED", "REJECTED")
        );

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public OrderSummaryResponseDto getOrderSummaryByDriver(Long driverId) {
        List<Order> orders = orderRepository.findByDriverIdAndStatusIn(
                driverId, List.of("DELIVERED", "REJECTED")
        );
        LocalDate now = LocalDate.now();

        Map<Integer, DailySummaryDto> dailyTrips = new HashMap<>();
        Map<Integer, MonthlySummaryDto> monthlyTrips = new HashMap<>();
        int totalOrders = 0;
        double totalEarnings = 0.0;
        double totalDistance = 0.0;

        for (Order order : orders) {
            if (order.getCreatedAt() == null || !driverId.equals(order.getDriverId())) continue;

            LocalDate createdDate = order.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int year = createdDate.getYear();
            int month = createdDate.getMonthValue();
            int day = createdDate.getDayOfMonth();

            // Monthly summary (current year)
            if (year == now.getYear()) {
                MonthlySummaryDto monthly = monthlyTrips.getOrDefault(month, new MonthlySummaryDto(0, 0.0, 0.0));
                monthly.setTripCount(monthly.getTripCount() + 1);
                monthly.setTotalEarnings(monthly.getTotalEarnings() + order.getAmount());
                monthly.setTotalDistance(monthly.getTotalDistance() + order.getDistance());
                monthlyTrips.put(month, monthly);
            }

            // Daily summary (current month & year)
            if (month == now.getMonthValue() && year == now.getYear()) {
                DailySummaryDto daily = dailyTrips.getOrDefault(day, new DailySummaryDto(0, 0.0, 0.0));
                daily.setTripCount(daily.getTripCount() + 1);
                daily.setTotalEarnings(daily.getTotalEarnings() + order.getAmount());
                daily.setTotalDistance(daily.getTotalDistance() + order.getDistance());
                dailyTrips.put(day, daily);
            }

            totalOrders++;
            totalEarnings += order.getAmount();
            totalDistance += order.getDistance();
        }

        return new OrderSummaryResponseDto(
                totalOrders,
                totalEarnings,
                totalDistance,
                dailyTrips,
                monthlyTrips
        );
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