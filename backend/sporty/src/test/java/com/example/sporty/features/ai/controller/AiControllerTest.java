package com.example.sporty.features.ai.controller;

import static org.mockito.Mockito.verifyNoInteractions;
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

/*
- AI-02 요청 입력값 검증 테스트
- AI는 Mock으로 대체하므로 API 키, DB 없이 실행된다.
*/
@ExtendWith(MockitoExtension.class)
class AiControllerTest {

    @Mock
    private MatchAiAgent matchAiAgent;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Spring 전체를 띄우지 않고 AiController만으로 HTTP 요청을 흉내 낸다.
        mockMvc = MockMvcBuilders.standaloneSetup(new AiController(matchAiAgent)).build();
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
}
