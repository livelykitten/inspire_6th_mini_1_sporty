package com.example.sporty.features.exerciseMatching.domain.dto;

import java.time.LocalDateTime;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.GenderGroup;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties (ignoreUnknown = true)
public class MatchResponseDto {

    private Long matchId;

    private String title;
    private String description;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime cancelDeadlineAt;

    private Integer numCurrentParticipant;
    private Integer maxParticipant;
    private Double distance;

    private MatchStatus status;
    private SkillLevel skillLevel;
    private SportType sportType;

    private GenderGroup genderGroup;
    private String region;
    @JsonProperty("isFree")
    private Boolean isFree;

    public static MatchResponseDto toResponseDto(MatchEntity entity) {
        return MatchResponseDto.builder()
            .matchId(entity.getId())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .startAt(entity.getStartAt())
            .endAt(entity.getEndAt())
            .cancelDeadlineAt(null) // TODO
            .numCurrentParticipant(null) // TODO
            .maxParticipant(entity.getMaxParticipant())
            .status(entity.getStatus())
            .skillLevel(entity.getSkillLevel())
            .sportType(entity.getSportType())
            .genderGroup(entity.getGenderGroup())
            .region("") // TODO
            .isFree(null) // TODO: ServiceEntity 연동 후 실제 유/무료 여부 매핑
            .distance(null) // TODO
            .build();
    }
}
