package com.example.sporty.features.exerciseMatching.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sporty.features.commons.exception.exerciseMatching.MatchNotFoundException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchAlreadyJoinedException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchAlreadyStartedException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchFullException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchRecruitmentClosedException;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchDetailResponseDto;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchParticipantResponseDto;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchParticipantSummaryDto;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchParticipantEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchParticipantRole;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.repository.MatchParticipantRepository;
import com.example.sporty.features.exerciseMatching.repository.MatchRepository;
import com.example.sporty.features.profiles.domain.entity.ProfileEntity;
import com.example.sporty.features.profiles.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final ProfileRepository profileRepository;

    // EM-06: 참가자 저장과 정원 도달 시 모집 마감을 하나의 트랜잭션으로 처리한다.
    @Transactional
    public MatchParticipantResponseDto joinMatch(Long matchId, Long userId) {
        MatchEntity match = matchRepository.findByIdForUpdate(matchId)
                .orElseThrow(MatchNotFoundException::new);

        // OWNER도 이미 등록된 참가자이므로 역할과 관계없이 중복 참여를 막는다.
        if (matchParticipantRepository.existsByMatch_IdAndUserId(matchId, userId)) {
            throw new MatchAlreadyJoinedException();
        }

        if (match.getStatus() != MatchStatus.RECRUITING) {
            throw new MatchRecruitmentClosedException();
        }

        // 잠금을 기다리는 동안 시작 시각이 지났을 수도 있으므로 조회 후 현재 시각을 확인한다.
        if (!match.getStartAt().isAfter(LocalDateTime.now())) {
            throw new MatchAlreadyStartedException();
        }

        long participantCount = matchParticipantRepository.countByMatch_Id(matchId);

        if (participantCount >= match.getMaxParticipant()) {
            throw new MatchFullException();
        }

        MatchParticipantEntity participant = matchParticipantRepository.save(
                MatchParticipantEntity.builder()
                        .match(match)
                        .userId(userId)
                        .role(MatchParticipantRole.PARTICIPANT)
                        .build());

        if (participantCount + 1 == match.getMaxParticipant()) {
            match.closeRecruitment();
        }

        return MatchParticipantResponseDto.builder()
                .matchParticipantId(participant.getId())
                .matchId(matchId)
                .userId(participant.getUserId())
                .role(participant.getRole())
                .build();
    }

    /**
     * EM-03: 기본 정보, 참가자 프로필과 조회 사용자의 참여 상태를 반환한다.
     * OWNER도 참가 인원에 포함하며, 비로그인 요청의 userId는 null이다.
     */
    public MatchDetailResponseDto getMatchDetail(Long matchId, Long userId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(MatchNotFoundException::new);

        List<MatchParticipantEntity> participants =
                matchParticipantRepository.findAllByMatch_IdOrderByIdAsc(matchId);
        List<Long> participantUserIds = participants.stream()
                .map(MatchParticipantEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, ProfileEntity> profilesByUserId = participantUserIds.isEmpty()
                ? Map.of()
                : profileRepository.findAllByUser_IdIn(participantUserIds).stream()
                        .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));

        List<MatchParticipantSummaryDto> participantSummaries = new ArrayList<>();
        boolean isOwner = false;
        boolean isParticipant = false;
        for (MatchParticipantEntity participant : participants) {
            ProfileEntity profile = participant.getUserId() == null
                    ? null : profilesByUserId.get(participant.getUserId());

            // 프로필이 누락되어도 참가 정보와 인원은 유지한다.
            participantSummaries.add(MatchParticipantSummaryDto.builder()
                    .profileId(profile == null ? null : profile.getId())
                    .nickname(profile == null ? null : profile.getNickname())
                    .imageUrl(profile == null ? null : profile.getImageUrl())
                    .role(participant.getRole())
                    .build());

            if (userId != null && userId.equals(participant.getUserId())) {
                isParticipant = true;
                if (participant.getRole() == MatchParticipantRole.OWNER) {
                    isOwner = true;
                }
            }
        }

        // TODO: Service/Location Entity 연동 후 장소 정보를 조회한다.
        // 시설 코드가 들어오기 전까지 serviceName/locationName/region은 null로 유지한다.
        return MatchDetailResponseDto.builder()
                .matchId(match.getId())
                .title(match.getTitle())
                .description(match.getDescription())
                .startAt(match.getStartAt())
                .endAt(match.getEndAt())
                .maxParticipant(match.getMaxParticipant())
                .currentParticipantCount(participants.size())
                .status(match.getStatus())
                .skillLevel(match.getSkillLevel())
                .sportType(match.getSportType())
                .serviceId(match.getServiceId())
                .participants(participantSummaries)
                .isOwner(isOwner)
                .isParticipant(isParticipant)
                .build();
    }
}
