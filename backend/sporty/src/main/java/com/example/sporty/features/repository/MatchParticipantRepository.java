package com.example.sporty.features.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sporty.features.exerciseMatching.domain.entity.MatchParticipantEntity;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipantEntity, Integer> {
    
}
