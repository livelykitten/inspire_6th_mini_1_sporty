package com.example.sporty.features.exerciseMatching.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * EM-03 운동 매칭 상세 화면에 필요한 응답 정보.
 * 장소/프로필 조회와 로그인 사용자 상태는 Service 구현 시 연결한다.
 */
@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MatchDetailResponseDto {

    private Integer matchId;
    private String title;
    private String description;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Integer maxParticipant;
    private Integer currentParticipantCount;

    private MatchStatus status;
    private SkillLevel skillLevel;
    private SportType sportType;

    private Integer serviceId;
    private String serviceName;
    private String locationName;
    private String region;

    private List<MatchParticipantSummaryDto> participants;

    // 현재 조회 사용자 기준. OWNER도 참가자이므로 두 값이 모두 true일 수 있다.
    @JsonProperty("isOwner")
    private Boolean isOwner;

    @JsonProperty("isParticipant")
    private Boolean isParticipant;
}
