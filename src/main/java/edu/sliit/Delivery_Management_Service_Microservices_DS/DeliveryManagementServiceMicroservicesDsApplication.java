package edu.sliit.Delivery_Management_Service_Microservices_DS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DeliveryManagementServiceMicroservicesDsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeliveryManagementServiceMicroservicesDsApplication.class, args);
	}

}
