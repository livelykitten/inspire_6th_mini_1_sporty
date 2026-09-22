package com.example.sporty.features.ai.agent;

import java.time.LocalDateTime;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.sporty.features.ai.util.PromptDateTable;
import com.example.sporty.features.exerciseMatching.domain.dto.MatchRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class MatchDraftAiAgent {

    private static final int DEFAULT_MATCH_HOURS = 2;
    private static final int TITLE_MAX = 50;
    private static final int DESCRIPTION_MAX = 500;
    private static final String NO_INFO_MESSAGE =
            "만들 매치 정보를 찾지 못했습니다. 종목, 지역, 날짜, 인원 중 하나 이상을 포함해 주세요. (예: 토요일 저녁 7시 송파에서 농구 10명)";

    private final ChatClient matchDraftChatClient;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public MatchRequestDto draft(String prompt) {
        System.out.println("debug >>>> match draft ai agent draft : " + prompt);

        String systemPrompt = """
                당신은 운동 매칭 서비스의 매치 개설 도우미입니다.

                ## 규칙
                1. 사용자의 문장에서 새로 개설할 매치 정보를 정리해 draftMatch 도구를 호출한다.
                2. 종목, 지역, 일시, 인원, 실력은 문장에 있는 것만 채운다. 추측하지 않는다.
                3. 지역은 서울시 자치구 이름으로 변환한다. 예: 강남역 → 강남구, 잠실 → 송파구
                4. 날짜는 직접 계산하지 않고 아래 날짜표에서 찾아 사용한다.
                5. 시각은 24시간제로 변환한다. 예: 저녁 7시 → 19:00
                6. 종료 시각을 말하지 않았으면 endAt은 채우지 않는다.
                7. title은 지역, 종목, 분위기를 담아 30자 안팎으로 작성한다.
                8. description은 참가자에게 보내는 친근한 안내를 2~3문장으로 작성한다.
                    문장에 없는 사실(가격, 시설, 준비물 등)은 지어내지 않는다.

                ## 날짜표
                %s
                """.formatted(PromptDateTable.of(PromptDateTable.today()));

        String result = matchDraftChatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();

        System.out.println("debug >>>> match draft ai agent result : " + result);

        MatchRequestDto draft = toDraft(result);
        if (draft == null || hasNoMatchInfo(draft)) {
            throw new AiDraftException(NO_INFO_MESSAGE);
        }
        return complete(draft);
    }

    // 폼에 바로 넣을 수 있게 보정: 종료 시각 기본값(시작 + 2시간), 글자 수 제한
    static MatchRequestDto complete(MatchRequestDto draft) {
        LocalDateTime endAt = draft.getEndAt();
        if (draft.getStartAt() != null && (endAt == null || !endAt.isAfter(draft.getStartAt()))) {
            endAt = draft.getStartAt().plusHours(DEFAULT_MATCH_HOURS);
        }
        return MatchRequestDto.builder()
                .sportType(draft.getSportType()).region(draft.getRegion())
                .startAt(draft.getStartAt()).endAt(endAt)
                .maxParticipant(draft.getMaxParticipant()).skillLevel(draft.getSkillLevel())
                .title(cut(draft.getTitle(), TITLE_MAX))
                .description(cut(draft.getDescription(), DESCRIPTION_MAX))
                .build();
    }

    // toDraft(): MatchAiAgent.toCondition()과 동일한 JSON 변환
    // hasNoMatchInfo(): 종목, 지역, 시작 일시, 인원, 실력이 모두 null (제목·설명은 AI가 항상 쓰므로 제외)
    // cut(): null이면 null, 길면 앞에서부터 자름
}
