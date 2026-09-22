package com.example.sporty.features.facility.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sporty.features.facility.dto.ServiceResponseDto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;



@RestController 
@RequestMapping("/api/services")
public class FacilityServiceController {

    //FC-01: 체육시설 목록 조회(검색 조건 포함)
    @GetMapping
    public ResponseEntity<List<ServiceResponseDto>> getServices() {
        List<ServiceResponseDto> mockList = new ArrayList<>();
        
        // Builder 패턴을 사용하여 객체 생성 (Setter 불필요)
        ServiceResponseDto mock1 = ServiceResponseDto.builder()
                .serviceId("S12345")
                .serviceName("마포구민체육센터 테니스장 주말 예약")
                .region("마포구")
                .status("접수중")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .build();
        
        ServiceResponseDto mock2 = ServiceResponseDto.builder()
                .serviceId("S12346")
                .serviceName("강남구 풋살장 평일 야간")
                .region("강남구")
                .status("예약마감")
                .startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(22, 0))
                .build();

        mockList.add(mock1);
        mockList.add(mock2);

        return ResponseEntity.ok(mockList);
    }
    
    
}
