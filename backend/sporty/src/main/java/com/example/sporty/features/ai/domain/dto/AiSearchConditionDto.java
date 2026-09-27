package com.example.sporty.features.ai.domain.dto;

import java.time.LocalDateTime;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

// AI가 문장에서 추출한 검색 조건 (tool → agent 내부 전달용)
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSearchConditionDto {

    private SportType sportType;
    private String region;
    private LocalDateTime startAt, endAt;
    private SkillLevel skillLevel;
    private MatchStatus status;
}
