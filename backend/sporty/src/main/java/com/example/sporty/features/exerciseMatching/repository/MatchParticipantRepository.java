package com.example.sporty.features.exerciseMatching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sporty.features.exerciseMatching.domain.entity.MatchParticipantEntity;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipantEntity, Long> {

    List<MatchParticipantEntity> findAllByMatch_IdOrderByIdAsc(Long matchId);

    // OWNER도 참가자로 등록되므로 역할 구분 없이 전체 참가 정보를 센다.
    long countByMatch_Id(Long matchId);

    boolean existsByMatch_IdAndUserId(Long matchId, Long userId);

    // 참가 정보를 조회한 뒤 getRole()로 OWNER/PARTICIPANT를 확인한다.
    Optional<MatchParticipantEntity> findByMatch_IdAndUserId(Long matchId, Long userId);
}
