package com.example.sporty.features.ai.tools;

import java.time.LocalDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchRequestDto;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;

public class MatchDraftAiTool {
    
    @Tool(description = "사용자의 문장에서 새로 개설할 운동 매치의 정보를 정리한다.", returnDirect = true)
    public MatchRequestDto draftMatch(
            @ToolParam(required = false, description = "운동 종목") SportType sportType,
            @ToolParam(required = false, description = "서울시 자치구 이름. 예: 강남구, 송파구") String region,
            @ToolParam(required = false, description = "매치 시작 일시") LocalDateTime startAt,
            @ToolParam(required = false, description = "매치 종료 일시") LocalDateTime endAt,
            @ToolParam(required = false, description = "모집 인원(명)") Integer maxParticipant,
            @ToolParam(required = false, description = "실력 수준") SkillLevel skillLevel,
            @ToolParam(required = false, description = "매치 제목") String title,
            @ToolParam(required = false, description = "참가자에게 보여줄 매치 상세 안내") String description) {

        MatchRequestDto draft = MatchRequestDto.builder()
                .sportType(sportType).region(region)
                .startAt(startAt).endAt(endAt)
                .maxParticipant(maxParticipant).skillLevel(skillLevel)
                .title(title).description(description)
                .build();

        System.out.println("debug >>>> ai tool draftMatch draft : " + draft);
        return draft;
    }
}
