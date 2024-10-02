package com.example.ABC.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> callXyzService() {
        String xyzUrl = "http://localhost:8081/perform-operation"; // Replace with actual XYZ endpoint
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(xyzUrl, String.class);

            // If HTTP status is 200, check the response body
            if (response.getStatusCode().is2xxSuccessful()) {
                // Extract statusCode if present
                String statusCode = extractStatusCode(response.getBody());

                if (statusCode != null) {
                    // Check if statusCode is not 0 or 1
                    if (!"0".equals(statusCode) && !"1".equals(statusCode)) {
                        logger.error("Invalid status code: {}", statusCode);
                        // Convert HttpStatusCode to HttpStatus and throw custom exception
                        HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
                        throw new CustomException(response.getBody(), httpStatus);
                    } else {
                        // Placeholder for your future implementation
                        logger.info("Valid status code (0 or 1): Placeholder for future code");
                        return new ResponseEntity<>(response.getBody(), response.getStatusCode());
                    }
                } else {
                    // If statusCode is not present, throw custom exception
                    logger.warn("No statusCode found in response. Throwing custom exception.");
                    HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
                    throw new CustomException(response.getBody(), httpStatus);
                }
            } else {
                // If HTTP status is not 200, throw an error
                HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
                throw new HttpStatusCodeException(httpStatus, response.getBody()) {};
            }
        } catch (HttpStatusCodeException ex) {
            // This will be handled in the global exception handler
            throw ex;
        }
    }

    /**
     * Helper method to extract 'statusCode' from the response body.
     */
    private String extractStatusCode(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Navigate to the 'statusCode' field, if present
            JsonNode statusCodeNode = rootNode.at("/getExtendedServiceAuthorizationResponse/status/statusCode");

            if (!statusCodeNode.isMissingNode()) {
                return statusCodeNode.asText();
            } else {
                logger.warn("statusCode not found in the response.");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while extracting statusCode: {}", e.getMessage());
            return null;
        }
    }
}
