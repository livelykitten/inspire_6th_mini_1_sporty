package com.example.sporty.features.exerciseMatching.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.sporty.features.exerciseMatching.domain.dto.MatchDetailResponseDto;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.repository.MatchParticipantRepository;
import com.example.sporty.features.exerciseMatching.repository.MatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    /**
     * EM-03: 매치 기본 정보와 OWNER를 포함한 현재 참가 인원을 반환한다.
     * 장소/프로필/인증 연동 전의 확장 필드는 null로 유지한다.
     */
    public MatchDetailResponseDto getMatchDetail(Integer matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "매치를 찾을 수 없습니다."));

        // OWNER가 이미 참가자 테이블에 있으므로 별도로 1을 더하지 않는다.
        int currentParticipantCount = Math.toIntExact(
                matchParticipantRepository.countByMatch_Id(matchId));

        // TODO: Service/Location Entity 연동 후 장소 정보를 조회한다.
        // TODO: User/Profile 연동 후 참가자 목록을 조회한다.
        // TODO: 인증 연동 후 현재 사용자의 OWNER/참가 여부를 확인한다.
        // 미연결 정보는 0, 빈 목록, false로 대체하지 않는다.
        return MatchDetailResponseDto.builder()
                .matchId(match.getId())
                .title(match.getTitle())
                .description(match.getDescription())
                .startAt(match.getStartAt())
                .endAt(match.getEndAt())
                .maxParticipant(match.getMaxParticipant())
                .currentParticipantCount(currentParticipantCount)
                .status(match.getStatus())
                .skillLevel(match.getSkillLevel())
                .sportType(match.getSportType())
                .serviceId(match.getServiceId())
                .build();
    }
}
