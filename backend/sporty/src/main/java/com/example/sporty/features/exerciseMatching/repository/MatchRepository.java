package com.example.sporty.features.exerciseMatching.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;

public interface MatchRepository extends JpaRepository<MatchEntity, Integer> {
}
