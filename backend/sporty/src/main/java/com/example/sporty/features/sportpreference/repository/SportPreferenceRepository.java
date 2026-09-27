package com.example.sporty.features.sportpreference.repository;

import com.example.sporty.features.sportpreference.domain.entity.SportPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SportPreferenceRepository extends JpaRepository<SportPreferenceEntity, Long> {

    List<SportPreferenceEntity> findByProfileId(Long profileId);

}
