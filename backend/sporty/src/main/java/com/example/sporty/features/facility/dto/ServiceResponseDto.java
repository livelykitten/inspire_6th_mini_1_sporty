package com.example.sporty.features.facility.dto;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Getter;

@Getter 
@Builder 
public class ServiceResponseDto {
    private String serviceId;
    private String serviceName;
    private String region;
    private String status;
    private LocalTime startTime;
    private LocalTime endTime;

}
