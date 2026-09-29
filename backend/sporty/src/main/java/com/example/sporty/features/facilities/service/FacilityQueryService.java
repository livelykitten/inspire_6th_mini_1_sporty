package com.example.sporty.features.facilities.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sporty.features.facilities.domain.dto.ServiceRequestDto;
import com.example.sporty.features.facilities.domain.dto.ServiceResponseDto;
import com.example.sporty.features.facilities.domain.entity.ServiceEntity;
import com.example.sporty.features.facilities.repository.ServiceRepository;
import com.example.sporty.features.facilities.specification.ServiceSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FacilityQueryService {

    private final ServiceRepository serviceRepository;

    public List<ServiceResponseDto> getFacilities(
            ServiceRequestDto request
    ) {
        validateTimeRange(request);

        List<ServiceEntity> services = serviceRepository.findAll(
                ServiceSpecification.search(request)
        );

        return services.stream()
                .map(ServiceResponseDto::from)
                .toList();
    }

    private void validateTimeRange(ServiceRequestDto request) {
        if (request.getStartTime() != null
                && request.getEndTime() != null
                && request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException(
                    "시작시간은 종료시간보다 늦을 수 없습니다."
            );
        }
    }
}
