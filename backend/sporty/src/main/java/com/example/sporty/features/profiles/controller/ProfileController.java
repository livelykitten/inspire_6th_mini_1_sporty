package com.example.sporty.features.profiles.controller;

import com.example.sporty.features.profiles.domain.dto.ProfileDetailResponseDto;
import com.example.sporty.features.profiles.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/profiles/{profileId}")
public class ProfileController {

    private final ProfileService profileService;

    // [PR-02] 프로필 조회
    @GetMapping
    public ResponseEntity<ProfileDetailResponseDto> getProfile(@PathVariable Long profileId) {

        ProfileDetailResponseDto response = profileService.getProfile(profileId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
