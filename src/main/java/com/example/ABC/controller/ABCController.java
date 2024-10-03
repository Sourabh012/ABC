package com.example.ABC.controller;

import com.example.ABC.service.MyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.exception.BackendServiceException;
import org.example.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ABCController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MyService service;

    @GetMapping("/call-xyz")
    public ResponseEntity<String> callXYZ() throws JsonProcessingException, CustomException {
        return service.callXyzService();
    }
}
