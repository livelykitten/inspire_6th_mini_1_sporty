package com.example.sporty.features.ai.agent;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.sporty.features.commons.exception.ai.AiSearchException;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MatchAiAgent {
    
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int CALENDAR_DAYS = 14;
    private static final String NO_CONDITION_MESSAGE =
            "검색 조건을 찾지 못했습니다. 종목, 지역, 날짜 중 하나 이상을 포함해 주세요. (예: 이번 주말 강남에서 풋살)";

    private final ChatClient matchChatClient;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public MatchRequestDto search(String prompt) {
        System.out.println("debug >>>> match ai agent search : " + prompt);

        LocalDate today = LocalDate.now(SEOUL);

        String systemPrompt = """
                당신은 운동 매칭 서비스의 검색 에이전트입니다.

                ## 규칙
                1. 사용자의 문장에서 검색 조건을 추출해 searchMatches 도구를 호출한다.
                2. 문장에 없는 조건은 채우지 않는다. 추측하지 않는다.
                3. 지역은 서울시 자치구 이름으로 변환한다. 예: 강남역 → 강남구, 잠실 → 송파구
                4. 날짜는 직접 계산하지 않고 아래 날짜표에서 찾아 사용한다.
                5. 기간 표현은 startAt을 시작일 00:00, endAt을 종료일 23:59로 한다.

                ## 날짜표
                이번 주말: %s
                %s
                """.formatted(thisWeekend(today), calendar(today));

        String result = matchChatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

        System.out.println("debug >>>> match ai agent search result : " + result);
        
        MatchRequestDto condition = toCondition(result);
        if (condition == null || hasNoCondition(condition)) {
            throw new AiSearchException(NO_CONDITION_MESSAGE);
        }
        return condition;
    }

    // tool이 호출되면 조건 JSON, 호출되지 않으면 AI의 안내 문장이 온다.
    private MatchRequestDto toCondition(String result) {
        if (result == null || result.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(result, MatchRequestDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // tool은 호출됐지만 추출된 조건이 하나도 없는 경우
    private boolean hasNoCondition(MatchRequestDto condition) {
        return condition.getSportType() == null
                && condition.getRegion() == null
                && condition.getStartAt() == null
                && condition.getEndAt() == null
                && condition.getSkillLevel() == null
                && condition.getStatus() == null;
    }

    // 오늘부터 14일간의 날짜와 요일 목록
    private String calendar(LocalDate today) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CALENDAR_DAYS; i++) {
            LocalDate date = today.plusDays(i);
            sb.append("- ").append(date).append(" (").append(dayName(date)).append(")");
            if (i == 0) sb.append(" 오늘");
            if (i == 1) sb.append(" 내일");
            sb.append("\n");
        }
        return sb.toString();
    }

    // 토~일. 오늘이 일요일이면 오늘 하루만
    private String thisWeekend(LocalDate today) {
        if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return today + " ~ " + today;
        }
        LocalDate saturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        return saturday + " ~ " + saturday.plusDays(1);
    }

    private String dayName(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
    }
}
