package com.example.sporty.features.facilities.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.sporty.features.facilities.domain.entity.ServiceEntity;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
}
