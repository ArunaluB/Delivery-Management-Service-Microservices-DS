package edu.sliit.Delivery_Management_Service_Microservices_DS.config;

public enum OrderStatus {
    NEW,
    DRIVER_ASSIGNMENT_IN_PROGRESS,
    NO_DRIVER_AVAILABLE,
    NO_DRIVER_ACCEPTED,
    ASSIGNED,
    DELIVERED,
    CANCELLED,
    ERROR,
    PENDING,
    ACCEPTED,
}
