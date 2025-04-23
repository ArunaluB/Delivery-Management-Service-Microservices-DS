package edu.sliit.Delivery_Management_Service_Microservices_DS.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusUpdateEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderRefId;
    private String status;
    private Long driverId;
    private Date timestamp;
}