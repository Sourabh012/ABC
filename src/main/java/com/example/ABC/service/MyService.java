package com.example.ABC.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;
import java.util.Map;

@Service
public class MyService {
    private static final Logger logger = LoggerFactory.getLogger(MyService.class);

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<String> callXyzService() throws CustomException {
        String xyzUrl = "http://localhost:8081/perform-operation"; // Replace with actual XYZ endpoint
        ResponseEntity<String> response;

        try {
            response = restTemplate.getForEntity(xyzUrl, String.class);

            // Extract statusCode if present
            String statusCode = extractStatusCode(response.getBody());

            if (statusCode != null) {
                if (!"0".equals(statusCode) && !"1".equals(statusCode)) {
                    logger.error("Invalid status code: {}", statusCode);
                    HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
                    HttpHeaders headers = response.getHeaders(); // Extract headers from XYZ response
                    throw new CustomException(response.getBody(), httpStatus, headers); // Pass headers to CustomException
                } else {
                    logger.info("Valid status code (0 or 1): Returning response as-is.");

                    // Return response body and headers from XYZ service
                    HttpHeaders headers = response.getHeaders();
                    return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
                }
            } else {
                logger.warn("No statusCode found in response. Throwing custom exception.");
                HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode().value());
                HttpHeaders headers = response.getHeaders(); // Extract headers
                throw new CustomException(response.getBody(), httpStatus, headers); // Pass headers to CustomException
            }
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            HttpStatus httpStatus = (HttpStatus) ex.getStatusCode();
            HttpHeaders headers = ex.getResponseHeaders(); // Extract headers from exception
            logger.error("Received {} from the XYZ service: {}", httpStatus, responseBody);
            throw new CustomException(responseBody, httpStatus, headers); // Pass headers to CustomException
        }
    }

    private String extractStatusCode(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode statusCodeNode = findStatusCodeField(rootNode);
            if (statusCodeNode != null && statusCodeNode.isTextual()) {
                return statusCodeNode.asText();
            } else if (statusCodeNode != null && statusCodeNode.isNumber()) {
                return statusCodeNode.asText();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Error while extracting statusCode: {}", e.getMessage());
            return null;
        }
    }

    private JsonNode findStatusCodeField(JsonNode node) {
        if (node.has("statusCode")) {
            return node.get("statusCode");
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode result = findStatusCodeField(field.getValue());
                if (result != null) {
                    return result;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                JsonNode result = findStatusCodeField(arrayElement);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}
