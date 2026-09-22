package com.example.sporty.features.exerciseMatching.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sporty.features.exerciseMatching.domain.dto.MatchCreateRequestDto;
import com.example.sporty.features.repository.MatchRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MatchService {
    private final MatchRepository matchRepository;
    

    @Transactional 
    public Integer createMatch(MatchCreateRequestDto req) {

        System.out.println("debug >> MatchService.createMatch(), req: " + req);

        // 1. get user id from the authentication context

        // 2. verify that the serviceId actually exists

        // 3. create MatchEntity and save

        // 4. create MatchParticipant entity and save  

        // 5. return matchId

        return null;
    }
}
