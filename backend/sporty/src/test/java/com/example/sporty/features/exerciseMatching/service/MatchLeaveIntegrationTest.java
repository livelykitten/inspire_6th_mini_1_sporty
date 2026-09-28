package com.example.sporty.features.exerciseMatching.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;
import com.example.sporty.features.users.domain.entity.UserEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.GenderGroup;
import com.example.sporty.support.MatchUserFixtures;
import com.example.sporty.support.FacilityFixtures;
import com.example.sporty.features.facilities.domain.entity.ServiceEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.sporty.features.commons.exception.exerciseMatching.MatchNotFoundException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchOwnerCannotLeaveException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchParticipantNotFoundException;
import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchParticipantEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchParticipantRole;
import com.example.sporty.features.exerciseMatching.repository.MatchParticipantRepository;
import com.example.sporty.features.exerciseMatching.repository.MatchRepository;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.auto_quote_keyword=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(MatchService.class)
// 테스트의 바깥 트랜잭션 없이 실제 Service의 커밋/롤백 결과를 조회한다.
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MatchLeaveIntegrationTest {

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatchParticipantRepository matchParticipantRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private Map<Long, UserEntity> users;
    private Long serviceId;

    private Long userId(long label) {
        return users.get(label).getId();
    }

    private Long matchId;
    private Long otherMatchId;
    private List<Long> participantIds;

    @BeforeEach
    void setUp() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            matchParticipantRepository.deleteAllInBatch();
            matchRepository.deleteAllInBatch();
            users = MatchUserFixtures.create(entityManager, 11);
            serviceId = FacilityFixtures.create(entityManager, FacilityFixtures.newServiceId()).getId();
            MatchEntity match = matchRepository.save(MatchEntity.builder()
                    .title("삭제 대상").sportType(SportType.FUTSAL)
                    .maxParticipant(5).genderGroup(GenderGroup.MIXED).service(entityManager.getReference(ServiceEntity.class, serviceId))
                    .startAt(LocalDateTime.now().plusDays(1)).endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                    .build());
            MatchEntity otherMatch = matchRepository.save(MatchEntity.builder()
                    .title("유지 대상").sportType(SportType.FUTSAL)
                    .maxParticipant(5).genderGroup(GenderGroup.MIXED).service(entityManager.getReference(ServiceEntity.class, serviceId))
                    .startAt(LocalDateTime.now().plusDays(1)).endAt(LocalDateTime.now().plusDays(1).plusHours(2))
                    .build());
            matchId = match.getId();
            otherMatchId = otherMatch.getId();
            participantIds = matchParticipantRepository.saveAll(List.of(
                    participant(match, 1L, MatchParticipantRole.OWNER),
                    participant(match, 2L, MatchParticipantRole.PARTICIPANT),
                    participant(match, 3L, MatchParticipantRole.PARTICIPANT)))
                    .stream().map(MatchParticipantEntity::getId).toList();
            matchParticipantRepository.save(participant(otherMatch, 1L, MatchParticipantRole.OWNER));
        });
    }

    @Test
    @DisplayName("[EM07-001] ?? ?? ? ??? ?? ??? ???? ??? ?? ???? ????")
    void leaveCommitsOnlyCurrentParticipantDeletion() {
        matchService.leaveMatch(matchId, userId(2L));

        assertThat(matchRepository.existsById(matchId)).isTrue();
        assertThat(matchParticipantRepository.existsById(participantIds.get(1))).isFalse();
        assertThat(matchParticipantRepository.findAllById(participantIds))
                .extracting(MatchParticipantEntity::getId)
                .containsExactlyInAnyOrder(participantIds.get(0), participantIds.get(2));
        assertThat(matchParticipantRepository.countByMatch_Id(otherMatchId)).isEqualTo(1);
        assertThat(matchRepository.existsById(otherMatchId)).isTrue();
        // ?? ?? ??? ?? ?? ??? ???? 404 ???.
        assertThatThrownBy(() -> matchService.leaveMatch(matchId, userId(2L)))
                .isInstanceOf(MatchParticipantNotFoundException.class);
    }

    @Test
    @DisplayName("[EM07-003, EM07-007] OWNER ?? ?? ? ???? ???? ?? ??? ??? ? ??")
    void ownerCanDeleteAfterRejectedLeave() {
        assertThatThrownBy(() -> matchService.leaveMatch(matchId, userId(1L)))
                .isInstanceOf(MatchOwnerCannotLeaveException.class);
        assertTargetMatchIsUnchanged();

        matchService.deleteMatch(matchId, userId(1L));

        assertThat(matchRepository.existsById(matchId)).isFalse();
        assertThat(matchParticipantRepository.countByMatch_Id(matchId)).isZero();
        assertThat(matchRepository.existsById(otherMatchId)).isTrue();
        assertThat(matchParticipantRepository.countByMatch_Id(otherMatchId)).isEqualTo(1);
    }

    @Test
    @DisplayName("[EM07-004, EM07-005] ????? ?? ??? ?? ?? ? DB? ????")
    void failedLeavePreservesData() {
        assertThatThrownBy(() -> matchService.leaveMatch(matchId, userId(4L)))
                .isInstanceOf(MatchParticipantNotFoundException.class);
        assertThatThrownBy(() -> matchService.leaveMatch(Long.MAX_VALUE, userId(2L)))
                .isInstanceOf(MatchNotFoundException.class);
        assertTargetMatchIsUnchanged();
    }

    private void assertTargetMatchIsUnchanged() {
        assertThat(matchRepository.existsById(matchId)).isTrue();
        assertThat(matchParticipantRepository.findAllById(participantIds))
                .extracting(MatchParticipantEntity::getId).containsExactlyInAnyOrderElementsOf(participantIds);
    }

    private MatchParticipantEntity participant(MatchEntity match, Long userId, MatchParticipantRole role) {
        return MatchParticipantEntity.builder().match(match).user(users.get(userId)).role(role).build();
    }
}
