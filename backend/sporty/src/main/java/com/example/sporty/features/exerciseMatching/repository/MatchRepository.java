package com.example.sporty.features.exerciseMatching.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;

import jakarta.persistence.LockModeType;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    // 같은 매치의 참여 요청은 앞선 트랜잭션이 끝난 뒤 인원과 중복 여부를 확인한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MatchEntity m where m.id = :matchId")
    Optional<MatchEntity> findByIdForUpdate(@Param("matchId") Long matchId);
}
