package com.example.sporty.features.exerciseMatching.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sporty.features.commons.exception.matches.MatchUserNotFoundException;
import com.example.sporty.features.commons.exception.matches.WithdrawnUserFoundException;
import com.example.sporty.features.commons.exception.matches.ServiceNotFoundException;
import com.example.sporty.features.facilities.repository.ServiceRepository;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchCreateRequestDto;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchResponseDto;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchSearchRequestDto;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchParticipantEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchParticipantRole;
import com.example.sporty.features.exerciseMatching.repository.MatchParticipantRepository;
import com.example.sporty.features.exerciseMatching.repository.MatchRepository;
import com.example.sporty.features.users.domain.entity.UserEntity;
import com.example.sporty.features.users.domain.entity.UserStatus;
import com.example.sporty.features.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.example.sporty.features.commons.exception.exerciseMatching.MatchNotFoundException;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchDetailResponseDto;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchParticipantSummaryDto;
import com.example.sporty.features.profiles.domain.entity.ProfileEntity;
import com.example.sporty.features.profiles.repository.ProfileRepository;

@Service 
@RequiredArgsConstructor 
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ServiceRepository serviceRepository;


    @Transactional 
    public Long createMatch(MatchCreateRequestDto req) {

        System.out.println("debug >> MatchService.createMatch(), req: " + req);

        // 1. get user id from the authentication context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.valueOf(auth.getName());


        UserEntity userEntity = 
            userRepository.findById(userId)
            .orElseThrow(() -> new MatchUserNotFoundException());

        if (userEntity.getStatus() != UserStatus.ACTIVE) {
            throw new WithdrawnUserFoundException();
        }

        // Validate the catalog before creating either match or participant records.
        Long serviceId = req.getServiceId();
        if (!serviceRepository.existsById(serviceId)) {
            throw new ServiceNotFoundException();
        }

        // 3. create MatchEntity and save
        MatchEntity match = req.toEntity(serviceId);
        MatchEntity savedMatchEntity =
            matchRepository.save(match);
        

        // 4. create MatchParticipant entity and save  

        MatchParticipantEntity matchParticipantEntity
            = MatchParticipantEntity.builder()
            .user(userEntity) 
            .match(savedMatchEntity)
            .role(MatchParticipantRole.OWNER)
            .build();
        
        matchParticipantRepository.save(matchParticipantEntity);

        
        // 5. return matchId
        return savedMatchEntity.getId();
    }

    public List<MatchResponseDto> searchMatches(MatchSearchRequestDto req) {
        System.out.println("debug >> MatchService.searchMatches(), req: " + req);

        return matchRepository.searchMatches(
            req.getServiceId(),
            escapeSearchKeyword(req.getTitleKeyword()),
            escapeSearchKeyword(req.getDescriptionKeyword()),
            req.getStartAt(),
            req.getEndAt(),
            req.getMaxParticipant(),
            req.getSkillLevel(),
            req.getSportType(),
            req.getGenderGroup()
        ).stream()
        .map(MatchResponseDto::toResponseDto)
        .toList();
    }

    private String escapeSearchKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }

        // Must match the repository's LIKE ESCAPE character; escape it first.
        return keyword.replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_");
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
                .map(MatchParticipantEntity::getUser)
                .filter(Objects::nonNull)
                .map(UserEntity::getId)
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
            ProfileEntity profile = participant.getUser().getId() == null
                    ? null : profilesByUserId.get(participant.getUser().getId());

            // 프로필이 누락되어도 참가 정보와 인원은 유지한다.
            participantSummaries.add(MatchParticipantSummaryDto.builder()
                    .profileId(profile == null ? null : profile.getId())
                    .nickname(profile == null ? null : profile.getNickname())
                    .imageUrl(profile == null ? null : profile.getImageUrl())
                    .role(participant.getRole())
                    .build());

            if (userId != null && userId.equals(participant.getUser().getId())) {
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
                .genderGroup(match.getGenderGroup())
                .serviceId(match.getServiceId())
                .participants(participantSummaries)
                .isOwner(isOwner)
                .isParticipant(isParticipant)
                .build();
    }
}
