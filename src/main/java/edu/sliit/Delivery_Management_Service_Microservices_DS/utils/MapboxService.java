//package edu.sliit.Delivery_Management_Service_Microservices_DS.utils;
//
//import org.springframework.stereotype.Service;
//
//
//@Service
//public class MapboxService {
//
//    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
//        final int EARTH_RADIUS_KM = 6371;
//
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lon2 - lon1);
//
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
//
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//
//        return EARTH_RADIUS_KM * c;
//    }
//}
package edu.sliit.Delivery_Management_Service_Microservices_DS.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MapboxService {
    private static final Logger logger = LoggerFactory.getLogger(MapboxService.class);

    // Maximum distance (in kilometers) a driver can be from the shop
    private static final double MAX_DRIVER_DISTANCE_KM = 5.0;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${mapbox.api.key}")
    private String mapboxApiKey;

    @Value("${mapbox.api.enabled:true}")
    private boolean mapboxApiEnabled;


    public MapboxService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Calculate distance between two coordinates using Mapbox API
     * Falls back to Haversine formula if API call fails
     *
     * @return distance in kilometers
     */
    public double calculateDistance(double originLat, double originLng, double destLat, double destLng) {
        if (mapboxApiEnabled) {
            try {
                // Try to use Mapbox API for more accurate distance
                return getDistanceFromMapboxApi(originLat, originLng, destLat, destLng);
            } catch (Exception e) {
                logger.warn("Error using Mapbox API, falling back to Haversine formula: {}", e.getMessage());
                // Fall back to Haversine formula
                return calculateHaversineDistance(originLat, originLng, destLat, destLng);
            }
        } else {
            // Use Haversine formula if API is disabled
            return calculateHaversineDistance(originLat, originLng, destLat, destLng);
        }
    }

    /**
     * Get distance using Mapbox Directions API
     */
//    private double getDistanceFromMapboxApi(double originLat, double originLng, double destLat, double destLng) throws JsonProcessingException {
//        try {
//            String url = UriComponentsBuilder
//                    .fromHttpUrl("https://api.mapbox.com/directions/v5/mapbox/driving")
//                    .pathSegment(originLng + "," + originLat + ";" + destLng + "," + destLat)
//                    .queryParam("access_token", mapboxApiKey)
//                    .queryParam("geometries", "geojson")
//                    .queryParam("overview", "simplified")
//                    .build()
//                    .toUriString();
//
//            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//            JsonNode root = objectMapper.readTree(response.getBody());
//
//            if (root.has("routes") && root.get("routes").size() > 0) {
//                // Extract distance in meters and convert to kilometers
//                double distanceInMeters = root.get("routes").get(0).get("distance").asDouble();
//                double distanceInKm = distanceInMeters / 1000.0;
//
//                logger.info("Mapbox API calculated distance: {} km between ({},{}) and ({},{})",
//                        distanceInKm, originLat, originLng, destLat, destLng);
//
//                return distanceInKm;
//            } else {
//                logger.warn("No routes found in Mapbox API response, falling back to Haversine");
//                return calculateHaversineDistance(originLat, originLng, destLat, destLng);
//            }
//        } catch (Exception e) {
//            logger.error("Error calling Mapbox API: {}", e.getMessage());
//            throw e;
//        }
//    }
    private double getDistanceFromMapboxApi(double originLat, double originLng, double destLat, double destLng) throws JsonProcessingException {
        try {
            // Validate coordinates before making the API call
            if (!areValidCoordinates(originLat, originLng) || !areValidCoordinates(destLat, destLng)) {
                logger.warn("Invalid coordinates detected. Origin: {},{} | Destination: {},{}",
                        originLat, originLng, destLat, destLng);
                return calculateHaversineDistance(originLat, originLng, destLat, destLng);
            }

            // Mapbox requires format: lng,lat;lng,lat
            String coordinates = String.format("%f,%f;%f,%f", originLng, originLat, destLng, destLat);

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.mapbox.com/directions/v5/mapbox/driving/" + coordinates)
                    .queryParam("access_token", mapboxApiKey)
                    .queryParam("geometries", "geojson")
                    .queryParam("overview", "simplified")
                    .build()
                    .toUriString();

            logger.debug("Calling Mapbox API with URL: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.has("routes") && root.get("routes").size() > 0) {
                double distanceInMeters = root.get("routes").get(0).get("distance").asDouble();
                double distanceInKm = distanceInMeters / 1000.0;

                logger.info("Mapbox API calculated distance: {} km between ({}, {}) and ({}, {})",
                        distanceInKm, originLat, originLng, destLat, destLng);

                return distanceInKm;
            } else {
                logger.warn("No routes found in Mapbox API response. Origin: {},{} | Destination: {},{}", originLat, originLng, destLat, destLng);
                return calculateHaversineDistance(originLat, originLng, destLat, destLng);
            }
        } catch (Exception e) {
            logger.error("Error calling Mapbox API: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Check if the provided coordinates are valid
     * @return true if coordinates appear to be valid
     */
    private boolean areValidCoordinates(double lat, double lng) {
        // Check for zero or near-zero coordinates (likely default values)
        if (Math.abs(lat) < 0.000001 && Math.abs(lng) < 0.000001) {
            return false;
        }

        // Basic coordinate range check
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }



    /**
     * Calculate distance using Haversine formula (as fallback)
     */
    public double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;

        try {
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = EARTH_RADIUS_KM * c;

            logger.debug("Haversine formula calculated distance: {} km between ({},{}) and ({},{})",
                    distance, lat1, lon1, lat2, lon2);

            return distance;
        } catch (Exception e) {
            logger.error("Error calculating distance with Haversine formula", e);
            return Double.MAX_VALUE; // Return maximum distance in case of error
        }
    }

    /**
     * Check if a driver is within the maximum allowed distance from the shop
     */
    public boolean isDriverWithinRange(double shopLat, double shopLng, double driverLat, double driverLng) {
        double distance = calculateDistance(shopLat, shopLng, driverLat, driverLng);
        return distance <= MAX_DRIVER_DISTANCE_KM;
    }

    /**
     * Get the maximum distance allowed for drivers
     */
    public double getMaxDriverDistance() {
        return MAX_DRIVER_DISTANCE_KM;
    }

    /**
     * Get estimated travel time between two points using Mapbox API
     * @return estimated travel time in minutes
     */
    public double getEstimatedTravelTime(double originLat, double originLng, double destLat, double destLng) {
        if (!mapboxApiEnabled) {
            logger.warn("Mapbox API is disabled, cannot get estimated travel time");
            return -1;
        }

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.mapbox.com/directions/v5/mapbox/driving")
                    .pathSegment(originLng + "," + originLat + ";" + destLng + "," + destLat)
                    .queryParam("access_token", mapboxApiKey)
                    .build()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.has("routes") && root.get("routes").size() > 0) {
                // Extract duration in seconds and convert to minutes
                double durationInSeconds = root.get("routes").get(0).get("duration").asDouble();
                double durationInMinutes = durationInSeconds / 60.0;

                logger.info("Estimated travel time: {} minutes", durationInMinutes);
                return durationInMinutes;
            } else {
                logger.warn("No routes found in Mapbox API response");
                return -1;
            }
        } catch (Exception e) {
            logger.error("Error getting estimated travel time from Mapbox API", e);
            return -1;
        }
    }
}