package com.example.sporty.features.sportpreference.repository;

import com.example.sporty.features.sportpreference.domain.entity.SportPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportPreferenceRepository extends JpaRepository<SportPreferenceEntity, Long> {

}
