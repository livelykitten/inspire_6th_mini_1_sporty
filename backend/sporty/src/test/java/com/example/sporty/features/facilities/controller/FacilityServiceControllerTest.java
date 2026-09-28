package com.example.sporty.features.facilities.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.sporty.features.commons.config.SecurityConfig;
import com.example.sporty.features.commons.filter.JwtAuthenticationFilter;
import com.example.sporty.features.facilities.domain.dto.ServiceRequestDto;
import com.example.sporty.features.facilities.domain.dto.ServiceResponseDto;
import com.example.sporty.features.facilities.service.FacilityQueryService;

@WebMvcTest(FacilityServiceController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@TestPropertySource(
        properties = "jwt.secret=test-only-signing-secret-with-at-least-32-bytes"
)
class FacilityServiceControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FacilityQueryService facilityQueryService;

    @Test
    @DisplayName("FC-01: 인증 없이 지역과 시설 종류로 목록을 조회한다")
    void getServicesWithSearchConditions() throws Exception {
        ServiceResponseDto response = ServiceResponseDto.builder()
                .serviceId("TEST-SERVICE-001")
                .serviceName("응봉공원 테니스장")
                .serviceType("테니스장")
                .region("성동구")
                .placeName("응봉공원")
                .contact("02-1234-5678")
                .status("접수중")
                .startTime(LocalTime.of(7, 0))
                .endTime(LocalTime.of(19, 0))
                .isFree(false)
                .url("https://example.com/reservation")
                .reservationDeadlineAt(
                        LocalDateTime.of(2026, 12, 31, 17, 0)
                )
                .build();

        when(facilityQueryService.getFacilities(any(ServiceRequestDto.class)))
                .thenReturn(List.of(response));

        mvc.perform(get("/api/services")
                        .param("region", "성동구")
                        .param("serviceType", "테니스장"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceId")
                        .value("TEST-SERVICE-001"))
                .andExpect(jsonPath("$[0].serviceType")
                        .value("테니스장"))
                .andExpect(jsonPath("$[0].region")
                        .value("성동구"))
                .andExpect(jsonPath("$[0].isFree")
                        .value(false));

        verify(facilityQueryService).getFacilities(
                argThat(request ->
                        "성동구".equals(request.getRegion())
                                && "테니스장".equals(
                                        request.getServiceType()
                                )
                )
        );
    }
}
