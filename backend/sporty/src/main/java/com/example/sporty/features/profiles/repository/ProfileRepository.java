package com.example.sporty.features.profiles.repository;

import com.example.sporty.features.profiles.domain.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
}
