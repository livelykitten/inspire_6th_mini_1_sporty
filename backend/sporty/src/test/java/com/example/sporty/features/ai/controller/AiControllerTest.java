package com.example.sporty.features.ai.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.sporty.features.ai.agent.MatchAiAgent;
import com.example.sporty.features.ai.agent.MatchDraftAiAgent;
import com.example.sporty.features.commons.exception.ai.AiDraftException;
import com.example.sporty.features.commons.exception.ai.AiSearchException;
import com.example.sporty.features.commons.handler.GlobalExceptionHandler;

/*
- AI-01, AI-02 요청 입력값 검증 테스트
- AI는 Mock으로 대체하므로 API 키, DB 없이 실행된다.
*/
@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private MatchAiAgent matchAiAgent;

    @Mock
    private MatchDraftAiAgent matchDraftAiAgent;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Spring 전체를 띄우지 않고 AiController만으로 HTTP 요청을 흉내 낸다.
        mockMvc = MockMvcBuilders.standaloneSetup(new AiController(matchAiAgent, matchDraftAiAgent))
                .setControllerAdvice(new GlobalExceptionHandler())   // 공통 핸들러 연결
                .build();
    }

    @Test
    @DisplayName("[TC-AI02-06] prompt가 비어 있으면 400과 안내 문구를 반환하고 AI를 호출하지 않는다")
    void returnsBadRequestWhenPromptIsBlank() throws Exception {
        mockMvc.perform(post("/api/ai/matches/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 내용을 입력해주세요."));

        verifyNoInteractions(matchAiAgent);
    }

    @Test
    @DisplayName("[TC-AI02-07] prompt가 500자를 넘으면 400과 안내 문구를 반환하고 AI를 호출하지 않는다")
    void returnsBadRequestWhenPromptIsTooLong() throws Exception {
        String longPrompt = "풋".repeat(501);

        mockMvc.perform(post("/api/ai/matches/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\": \"" + longPrompt + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청은 500자 이하로 입력해주세요."));

        verifyNoInteractions(matchAiAgent);
    }

    @Test
    @DisplayName("[TC-AI02-08] AI가 검색 조건을 찾지 못하면 400과 안내 문구를 반환한다")
    void returnsBadRequestWhenNoCondition() throws Exception {
        when(matchAiAgent.search("같이 운동할 사람 있나요"))
                .thenThrow(new AiSearchException("검색 조건을 찾지 못했습니다."));

        mockMvc.perform(post("/api/ai/matches/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\": \"같이 운동할 사람 있나요\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("검색 조건을 찾지 못했습니다."));
    }

    @Test
    @DisplayName("[TC-AI01-01] 초안 생성 prompt가 비어 있으면 400과 안내 문구를 반환하고 AI를 호출하지 않는다")
    void returnsBadRequestWhenDraftPromptIsBlank() throws Exception {
        mockMvc.perform(post("/api/ai/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("요청 내용을 입력해주세요."));

        verifyNoInteractions(matchDraftAiAgent);
    }

    @Test
    @DisplayName("[TC-AI01-02] AI가 매치 정보를 찾지 못하면 400과 안내 문구를 반환한다")
    void returnsBadRequestWhenNoMatchInfo() throws Exception {
        when(matchDraftAiAgent.draft("같이 운동해요"))
                .thenThrow(new AiDraftException("만들 매치 정보를 찾지 못했습니다."));

        mockMvc.perform(post("/api/ai/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\": \"같이 운동해요\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("만들 매치 정보를 찾지 못했습니다."));
    }
}
