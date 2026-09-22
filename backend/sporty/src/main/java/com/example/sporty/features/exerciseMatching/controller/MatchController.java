package com.example.sporty.features.exerciseMatching.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sporty.features.exerciseMatching.domain.dto.MatchDetailResponseDto;
import com.example.sporty.features.exerciseMatching.service.MatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    // EM-03: 비로그인 조회도 허용하며, 로그인한 경우 내 생성자/참여 여부를 함께 반환한다.
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDetailResponseDto> getMatchDetail(
            @PathVariable("matchId") Integer matchId,
            @AuthenticationPrincipal Integer userId) {
        return ResponseEntity.ok(matchService.getMatchDetail(matchId, userId));
    }

    // EM-05: 생성자 전용 삭제. 구현 후 204, 인증/권한/매치 확인 실패 시 401/403/404.
    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable("matchId") Integer matchId) {
        // TODO: 인증된 사용자 정보 전달 및 Service의 OWNER 권한 확인 연결.
        // TODO: 매치와 연결된 참가자 삭제 후 204 반환.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // EM-06: 매치 참여. 구현 후 201 + MatchParticipantResponse.
    // 모집 마감, 정원 초과/인증 실패/매치 없음/이미 참가 중 일 때 400/401/404/409.
    @PostMapping("/{matchId}/participants")
    public ResponseEntity<Void> joinMatch(@PathVariable("matchId") Integer matchId) {
        // TODO: 인증된 사용자 정보 전달 및 Service 참여 처리 연결.
        // TODO: 모집 마감/정원 초과 400, 인증 실패 401, 매치 없음 404, 중복 참여 409 처리.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // EM-07: 운동 매치 탈퇴. 구현 후 204,
    // 인증실패/매치 생성자 탈퇴 불가/매치 또는 참가정보 없음 일 때 401/403/404
    @DeleteMapping("/{matchId}/participants/me")
    public ResponseEntity<Void> leaveMatch(@PathVariable("matchId") Integer matchId) {
        // TODO: 인증된 사용자 정보 전달 및 Service 탈퇴 처리 연결.
        // TODO: 인증 실패 401, 매치/참가 정보 없음 404 처리. 일반 참가자는 경기 시작 후에도 탈퇴 가능.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
