package com.example.sporty.features.facilities.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sporty.features.facilities.domain.dto.ServiceRequestDto;
import com.example.sporty.features.facilities.domain.dto.ServiceResponseDto;
import com.example.sporty.features.facilities.service.FacilityQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class FacilityServiceController {

    private final FacilityQueryService facilityQueryService;

    // FC-01: 체육시설 목록 조회
    @GetMapping
    public ResponseEntity<List<ServiceResponseDto>> getFacilities(
            @ModelAttribute ServiceRequestDto request
    ) {
        return ResponseEntity.ok(
                facilityQueryService.getFacilities(request)
        );
    }

    
    
}
