package com.example.sporty.features.exerciseMatching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sporty.features.commons.exception.exerciseMatching.MatchNotFoundException;
import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchDetailResponseDto;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchParticipantEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchParticipantRole;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchStatus;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;
import com.example.sporty.features.exerciseMatching.repository.MatchParticipantRepository;
import com.example.sporty.features.exerciseMatching.repository.MatchRepository;
import com.example.sporty.features.profiles.domain.entity.ProfileEntity;
import com.example.sporty.features.profiles.repository.ProfileRepository;
import com.example.sporty.features.users.domain.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private MatchParticipantRepository matchParticipantRepository;

    @Mock
    private ProfileRepository profileRepository;

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
        when(matchParticipantRepository.findAllByMatch_IdOrderByIdAsc(101)).thenReturn(List.of(
                participant(1, MatchParticipantRole.OWNER),
                participant(2, MatchParticipantRole.PARTICIPANT)));
        // 프로필 조회 순서가 달라도 사용자 ID로 연결해야 한다.
        when(profileRepository.findAllByUser_IdIn(List.of(1L, 2L))).thenReturn(List.of(
                profile(2, 402L, "참가자", null),
                profile(1, 401L, "생성자", "https://example.com/owner.png")));

        MatchDetailResponseDto response = matchService.getMatchDetail(101, null);

        assertThat(response.getMatchId()).isEqualTo(101);
        assertThat(response.getTitle()).isEqualTo("주말 풋살 모집");
        assertThat(response.getDescription()).isEqualTo("같이 풋살하실 분");
        assertThat(response.getStartAt()).isEqualTo(startAt);
        assertThat(response.getEndAt()).isEqualTo(endAt);
        assertThat(response.getMaxParticipant()).isEqualTo(10);
        assertThat(response.getCurrentParticipantCount()).isEqualTo(2);
        assertThat(response.getStatus()).isEqualTo(MatchStatus.CLOSED);
        assertThat(response.getSkillLevel()).isEqualTo(SkillLevel.INTERMEDIATE);
        assertThat(response.getSportType()).isEqualTo(SportType.FUTSAL);
        assertThat(response.getServiceId()).isEqualTo(7);

        assertThat(response.getParticipants()).hasSize(2);
        assertThat(response.getParticipants().get(0).getProfileId()).isEqualTo(401L);
        assertThat(response.getParticipants().get(0).getNickname()).isEqualTo("생성자");
        assertThat(response.getParticipants().get(0).getImageUrl()).isEqualTo("https://example.com/owner.png");
        assertThat(response.getParticipants().get(0).getRole()).isEqualTo(MatchParticipantRole.OWNER);
        assertThat(response.getParticipants().get(1).getProfileId()).isEqualTo(402L);
        assertThat(response.getParticipants().get(1).getNickname()).isEqualTo("참가자");
        assertThat(response.getParticipants().get(1).getImageUrl()).isNull();
        assertThat(response.getParticipants().get(1).getRole()).isEqualTo(MatchParticipantRole.PARTICIPANT);
        assertThat(response.getIsOwner()).isFalse();
        assertThat(response.getIsParticipant()).isFalse();
        // 시설 코드 연동 전에는 실제 장소를 임의로 채우지 않는다.
        assertThat(response.getServiceName()).isNull();
        assertThat(response.getLocationName()).isNull();
        assertThat(response.getRegion()).isNull();
        verify(profileRepository).findAllByUser_IdIn(List.of(1L, 2L));
    }

    @Test
    @DisplayName("[추가 검증] 참가 정보가 없으면 현재 인원은 0이며 생성자 수를 임의로 더하지 않는다")
    void getMatchDetailReturnsZeroWhenNoParticipantsAreStored() {
        MatchEntity match = MatchEntity.builder().id(101).build();
        when(matchRepository.findById(101)).thenReturn(Optional.of(match));
        when(matchParticipantRepository.findAllByMatch_IdOrderByIdAsc(101)).thenReturn(List.of());

        MatchDetailResponseDto response = matchService.getMatchDetail(101, 1);

        assertThat(response.getCurrentParticipantCount()).isZero();
        assertThat(response.getParticipants()).isEmpty();
        assertThat(response.getIsOwner()).isFalse();
        assertThat(response.getIsParticipant()).isFalse();
        verifyNoInteractions(profileRepository);
    }

    @Test
    @DisplayName("[TC-EM03-02] 존재하지 않는 매칭 상세 조회 - 404 예외가 발생한다")
    void getMatchDetailThrowsNotFoundWhenMatchDoesNotExist() {
        when(matchRepository.findById(999999)).thenReturn(Optional.empty());

        MatchNotFoundException exception = assertThrows(MatchNotFoundException.class,
                () -> matchService.getMatchDetail(999999, null));

        assertThat(exception.getMessage()).isEqualTo("매치를 찾을 수 없습니다.");
        verifyNoInteractions(matchParticipantRepository, profileRepository);
    }

    @ParameterizedTest
    @CsvSource({"1,true,true", "2,false,true", "3,false,false"})
    @DisplayName("조회 사용자의 해당 매치 참가 역할로 생성자와 참여 여부를 구분한다")
    void getMatchDetailReturnsCurrentUserState(Integer userId, boolean owner, boolean joined) {
        when(matchRepository.findById(101)).thenReturn(Optional.of(MatchEntity.builder().id(101).build()));
        when(matchParticipantRepository.findAllByMatch_IdOrderByIdAsc(101)).thenReturn(List.of(
                participant(1, MatchParticipantRole.OWNER),
                participant(2, MatchParticipantRole.PARTICIPANT)));
        when(profileRepository.findAllByUser_IdIn(List.of(1L, 2L))).thenReturn(List.of());

        MatchDetailResponseDto response = matchService.getMatchDetail(101, userId);

        assertThat(response.getIsOwner()).isEqualTo(owner);
        assertThat(response.getIsParticipant()).isEqualTo(joined);
    }

    @Test
    @DisplayName("프로필이 없어도 참가자 수와 역할을 유지하고 개인정보 필드는 null로 반환한다")
    void missingProfileDoesNotRemoveParticipant() {
        when(matchRepository.findById(101)).thenReturn(Optional.of(MatchEntity.builder().id(101).build()));
        when(matchParticipantRepository.findAllByMatch_IdOrderByIdAsc(101))
                .thenReturn(List.of(participant(1, MatchParticipantRole.OWNER)));
        when(profileRepository.findAllByUser_IdIn(List.of(1L))).thenReturn(List.of());

        MatchDetailResponseDto response = matchService.getMatchDetail(101, 1);

        assertThat(response.getCurrentParticipantCount()).isEqualTo(1);
        assertThat(response.getParticipants()).hasSize(1);
        assertThat(response.getParticipants().get(0).getProfileId()).isNull();
        assertThat(response.getParticipants().get(0).getNickname()).isNull();
        assertThat(response.getParticipants().get(0).getImageUrl()).isNull();
        assertThat(response.getParticipants().get(0).getRole()).isEqualTo(MatchParticipantRole.OWNER);
        assertThat(response.getIsOwner()).isTrue();
        assertThat(response.getIsParticipant()).isTrue();
    }

    @Test
    @DisplayName("프로필 ID는 Long 값을 잘라내지 않고 반환한다")
    void profileIdRetainsLongValue() {
        when(matchRepository.findById(101)).thenReturn(Optional.of(MatchEntity.builder().id(101).build()));
        when(matchParticipantRepository.findAllByMatch_IdOrderByIdAsc(101))
                .thenReturn(List.of(participant(1, MatchParticipantRole.OWNER)));
        when(profileRepository.findAllByUser_IdIn(List.of(1L)))
                .thenReturn(List.of(profile(1, 2147483648L, "생성자", null)));

        assertThat(matchService.getMatchDetail(101, null).getParticipants().get(0).getProfileId())
                .isEqualTo(2147483648L);
    }

    private MatchParticipantEntity participant(Integer userId, MatchParticipantRole role) {
        return MatchParticipantEntity.builder().userId(userId).role(role).build();
    }

    private ProfileEntity profile(Integer userId, Long profileId, String nickname, String imageUrl) {
        return ProfileEntity.builder()
                .id(profileId)
                .user(UserEntity.builder().id(userId.longValue()).build())
                .nickname(nickname)
                .imageUrl(imageUrl)
                .build();
    }
}
