package com.example.sporty.features.exerciseMatching.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    @Query ("""
            SELECT m
            FROM MatchEntity m
            WHERE (:serviceId IS NULL OR m.serviceId = :serviceId)
            AND (:titleKeyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :titleKeyword, '%')) ESCAPE '!')
            AND (:descriptionKeyword IS NULL OR LOWER(m.description) LIKE LOWER(CONCAT('%', :descriptionKeyword, '%')) ESCAPE '!')
            AND (:startAt IS NULL OR m.startAt >= :startAt)
            AND (:endAt IS NULL OR m.endAt <= :endAt)
            AND (:maxParticipant IS NULL OR m.maxParticipant <= :maxParticipant)
            AND (:skillLevel IS NULL OR m.skillLevel = :skillLevel)
            AND (:sportType IS NULL OR m.sportType = :sportType)
            """)
    List<MatchEntity> searchMatches(
        @Param ("serviceId") Long serviceId,
        @Param("titleKeyword") String titleKeyword,
        @Param("descriptionKeyword") String descriptionKeyword,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt,
        @Param("maxParticipant") Integer maxParticipant,
        @Param("skillLevel") SkillLevel skillLevel,
        @Param("sportType") SportType sportType
    );
}