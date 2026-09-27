package com.example.sporty.features.profiles.service;

import com.example.sporty.features.commons.exception.profiles.ProfileNotFoundException;
import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.profiles.domain.dto.ProfileDetailResponseDto;
import com.example.sporty.features.profiles.domain.entity.ProfileEntity;
import com.example.sporty.features.profiles.repository.ProfileRepository;
import com.example.sporty.features.sportpreference.domain.entity.SportPreferenceEntity;
import com.example.sporty.features.sportpreference.repository.SportPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final SportPreferenceRepository sportPreferenceRepository;

    // [PR-02] 프로필 조회
    @Transactional(readOnly = true)
    public ProfileDetailResponseDto getProfile(Long profileId) {

        // 1. 프로필 ID로 프로필 엔티티 얻기
        ProfileEntity profile
                = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException());

        // 2. 프로필 ID로 회원 선호 종목들 얻기
        List<SportType> sportTypes =
                sportPreferenceRepository.findByProfileId(profileId)
                        .stream()
                        .map(SportPreferenceEntity::getSportType)
                        .toList();

        return ProfileDetailResponseDto.fromEntity(profile, sportTypes);
    }

}
