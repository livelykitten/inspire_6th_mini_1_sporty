package com.example.sporty.features.exerciseMatching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchDetailResponseDto;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;
import com.example.sporty.features.exerciseMatching.repository.MatchParticipantRepository;
import com.example.sporty.features.exerciseMatching.repository.MatchRepository;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchParticipantRepository matchParticipantRepository;

    @InjectMocks
    private MatchService matchService;

    @Test
    @DisplayName("[TC-EM03-01] 매칭 상세 정상 조회 - 기본 정보와 조회된 참가 인원을 반환한다")
    void getMatchDetailReturnsStoredMatchInformation() {
        LocalDateTime startAt = LocalDateTime.of(2026, 9, 26, 19, 0);
        LocalDateTime endAt = startAt.plusHours(2);
        MatchEntity match = MatchEntity.builder()
                .id(101)
                .title("주말 풋살 모집")
                .description("같이 풋살하실 분")
                .startAt(startAt)
                .endAt(endAt)
                .maxParticipant(10)
                .status(MatchStatus.CLOSED)
                .skillLevel(SkillLevel.INTERMEDIATE)
                .sportType(SportType.FUTSAL)
                .serviceId(7)
                .build();
        when(matchRepository.findById(101)).thenReturn(Optional.of(match));
        when(matchParticipantRepository.countByMatch_Id(101)).thenReturn(3L);

        MatchDetailResponseDto response = matchService.getMatchDetail(101);

        assertThat(response.getMatchId()).isEqualTo(101);
        assertThat(response.getTitle()).isEqualTo("주말 풋살 모집");
        assertThat(response.getDescription()).isEqualTo("같이 풋살하실 분");
        assertThat(response.getStartAt()).isEqualTo(startAt);
        assertThat(response.getEndAt()).isEqualTo(endAt);
        assertThat(response.getMaxParticipant()).isEqualTo(10);
        assertThat(response.getCurrentParticipantCount()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(MatchStatus.CLOSED);
        assertThat(response.getSkillLevel()).isEqualTo(SkillLevel.INTERMEDIATE);
        assertThat(response.getSportType()).isEqualTo(SportType.FUTSAL);
        assertThat(response.getServiceId()).isEqualTo(7);

        // 미연결 정보를 참가자 없음이나 미참여 상태로 잘못 표현하지 않는다.
        assertThat(response.getParticipants()).isNull();
        assertThat(response.getIsOwner()).isNull();
        assertThat(response.getIsParticipant()).isNull();
        assertThat(response.getServiceName()).isNull();
        assertThat(response.getLocationName()).isNull();
        assertThat(response.getRegion()).isNull();
    }

    @Test
    @DisplayName("[추가 검증] 참가 정보가 없으면 현재 인원은 0이며 생성자 수를 임의로 더하지 않는다")
    void getMatchDetailReturnsZeroWhenNoParticipantsAreStored() {
        MatchEntity match = MatchEntity.builder().id(101).build();
        when(matchRepository.findById(101)).thenReturn(Optional.of(match));
        when(matchParticipantRepository.countByMatch_Id(101)).thenReturn(0L);

        MatchDetailResponseDto response = matchService.getMatchDetail(101);

        assertThat(response.getCurrentParticipantCount()).isZero();
    }

    @Test
    @DisplayName("[TC-EM03-02] 존재하지 않는 매칭 상세 조회 - 404 예외가 발생한다")
    void getMatchDetailThrowsNotFoundWhenMatchDoesNotExist() {
        when(matchRepository.findById(999999)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> matchService.getMatchDetail(999999));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("매치를 찾을 수 없습니다.");
        verifyNoInteractions(matchParticipantRepository);
    }
}
