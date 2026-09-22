package com.example.sporty.features.ai.agent;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MatchAiAgent {
    
    private final ChatClient matchChatClient;

    public String search(String prompt) {
        System.out.println("debug >>>> match ai agent search : " + prompt);

        String systemPrompt = """
                당신은 운동 매칭 서비스의 검색 에이전트입니다.

                ## 규칙
                1. 사용자의 문장에서 검색 조건을 추출해 searchMatches 도구를 호출한다.
                2. 문장에 없는 조건은 채우지 않는다. 추측하지 않는다.
                3. 지역은 서울시 자치구 이름으로 변환한다. 예: 강남역 → 강남구, 잠실 → 송파구
                4. 날짜는 오늘을 기준으로 계산한다. 오늘: %s
                5. 기간 표현(이번 주말 등)은 startAt을 시작일 00:00, endAt을 종료일 23:59로 한다.
                """.formatted(today());

        String result = matchChatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

        System.out.println("debug >>>> match ai agent search result : " + result);
        return result;
    }

    private String today() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return now + " (" + now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + ")";
    }
}
