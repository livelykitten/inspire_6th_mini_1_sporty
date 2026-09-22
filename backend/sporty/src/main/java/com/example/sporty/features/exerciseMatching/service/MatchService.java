package com.example.sporty.features.exerciseMatching.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sporty.features.commons.exception.matches.MatchUserNotFoundException;
import com.example.sporty.features.commons.exception.matches.WithdrawnUserFoundException;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchCreateRequestDto;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchParticipantEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.MatchParticipantRole;
import com.example.sporty.features.repository.MatchParticipantRepository;
import com.example.sporty.features.repository.MatchRepository;
import com.example.sporty.features.users.domain.entity.UserEntity;
import com.example.sporty.features.users.domain.entity.UserStatus;
import com.example.sporty.features.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchParticipantRepository matchParticipantRepository;
    private final UserRepository userRepository;


    @Transactional 
    public Integer createMatch(MatchCreateRequestDto req) {

        System.out.println("debug >> MatchService.createMatch(), req: " + req);

        // 1. get user id from the authentication context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = Integer.valueOf(auth.getName());


        UserEntity userEntity = 
            userRepository.findById((long)userId)
            .orElseThrow(() -> new MatchUserNotFoundException());

        if (userEntity.getStatus() != UserStatus.ACTIVE) {
            throw new WithdrawnUserFoundException();
        }

        // 2. verify that the serviceId actually exists
        // TODO: ServiceRepository가 구현되면, 실제 조회하여
        // ServiceEntity 객체 불러오기
        Integer serviceId = req.getServiceId();

        // 3. create MatchEntity and save
        // TODO: 2번에서 불러온 ServiceEntity를 인자로 주기
        MatchEntity match = req.toEntity(serviceId);
        MatchEntity savedMatchEntity =
            matchRepository.save(match);
        

        // 4. create MatchParticipant entity and save  

        MatchParticipantEntity matchParticipantEntity
            = MatchParticipantEntity.builder()
            .user(userEntity) 
            .match(savedMatchEntity)
            .role(MatchParticipantRole.OWNER)
            .build();
        
        matchParticipantRepository.save(matchParticipantEntity);

        
        // 5. return matchId
        return savedMatchEntity.getId();
    }
}
