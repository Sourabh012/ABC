package com.example.ABC.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.BackendServiceException;
import org.example.model.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class MyService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;


    public ResponseEntity<String> abcService() throws JsonProcessingException {
        String url = "http://localhost:8081/perform-operation";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException ex) {
            String errorResponseBody = ex.getResponseBodyAsString();
            int httpCode = Integer.parseInt(ErrorUtils.extractValue(errorResponseBody, "httpCode"));
            String httpMessage = ErrorUtils.extractValue(errorResponseBody, "httpMessage");
            String moreInformation = ErrorUtils.extractValue(errorResponseBody, "moreInformation");

            throw new BackendServiceException(
                    httpCode,
                    httpMessage,
                    moreInformation
            );
        }
    }

    public class ErrorUtils {
        public static String extractValue(String source, String fieldName) {
            String prefix = fieldName + "=";
            int start = source.indexOf(prefix) + prefix.length();
            int end = source.indexOf(",", start);
            if (start > -1 && end > -1) {
                return source.substring(start, end).replaceAll("'", "").trim();
            }
            return "Unknown";
        }
    }

}
