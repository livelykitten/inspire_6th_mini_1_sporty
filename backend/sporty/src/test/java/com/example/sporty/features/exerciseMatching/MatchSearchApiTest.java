package com.example.sporty.features.exerciseMatching;

import com.example.sporty.features.commons.util.SportType;
import com.example.sporty.features.exerciseMatching.controller.MatchController;
import com.example.sporty.features.exerciseMatching.domain.entity.MatchEntity;
import com.example.sporty.features.exerciseMatching.domain.enums.SkillLevel;
import com.example.sporty.features.exerciseMatching.repository.MatchParticipantRepository;
import com.example.sporty.features.exerciseMatching.repository.MatchRepository;
import com.example.sporty.features.exerciseMatching.service.MatchService;
import com.example.sporty.features.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP binding/validation through the real service and JPQL repository against isolated H2.
 * Security filters and the production MariaDB configuration are outside this test's scope.
 */
class MatchSearchApiTest {
    private static SessionFactory factory;
    private static LocalValidatorFactoryBean validator;
    private EntityManager em;
    private MockMvc mvc;

    @BeforeAll
    static void initializeDatabase() {
        factory = new Configuration()
            .addAnnotatedClass(MatchEntity.class)
            .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
            .setProperty("hibernate.connection.url", "jdbc:h2:mem:match-search;MODE=MariaDB;DB_CLOSE_DELAY=-1")
            .setProperty("hibernate.hbm2ddl.auto", "create-drop")
            .buildSessionFactory();
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
    }

    @AfterAll
    static void closeDatabase() {
        if (validator != null) validator.close();
        if (factory != null) factory.close();
    }

    @BeforeEach
    void setUp() {
        em = factory.createEntityManager();
        em.getTransaction().begin();
        MatchRepository repository = new JpaRepositoryFactory(em).getRepository(MatchRepository.class);
        MatchService service = new MatchService(repository,
            mock(MatchParticipantRepository.class), mock(UserRepository.class));
        mvc = MockMvcBuilders.standaloneSetup(new MatchController(service))
            .setValidator(validator).build();
    }

    @AfterEach
    void rollback() {
        if (em != null) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            em.close();
        }
    }

    private MatchEntity.MatchEntityBuilder match(String title) {
        return MatchEntity.builder().serviceId(1).title(title).description("초보 환영")
            .startAt(LocalDateTime.parse("2026-09-26T10:00:00"))
            .endAt(LocalDateTime.parse("2026-09-26T12:00:00"))
            .maxParticipant(10).skillLevel(SkillLevel.BEGINNER).sportType(SportType.FUTSAL);
    }

    private int save(MatchEntity.MatchEntityBuilder builder) {
        MatchEntity entity = builder.build();
        em.persist(entity);
        em.flush();
        return entity.getId();
    }

    private void expectMatches(MockHttpServletRequestBuilder request, Integer... ids) throws Exception {
        // Exact membership detects both missing matches and incorrectly included matches.
        em.clear();
        mvc.perform(request).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.length()").value(ids.length))
            .andExpect(jsonPath("$[*].matchId", containsInAnyOrder(ids)));
    }

    @Test
    @DisplayName("TC-EM02-01 전체 운동 매칭 목록 조회")
    void allMatches() throws Exception {
        int a = save(match("주말 풋살"));
        int b = save(match("농구").sportType(SportType.BASKETBALL).description(null));
        expectMatches(get("/api/matches"), a, b);
        mvc.perform(get("/api/matches").param("titleKeyword", "주말"))
            .andExpect(jsonPath("$[0].title").value("주말 풋살"))
            .andExpect(jsonPath("$[0].description").value("초보 환영"))
            .andExpect(jsonPath("$[0].sportType").value("FUTSAL"))
            .andExpect(jsonPath("$[0].skillLevel").value("BEGINNER"))
            .andExpect(jsonPath("$[0].maxParticipant").value(10))
            .andExpect(jsonPath("$[0].status").value("RECRUITING"));
    }

    @Test
    @DisplayName("TC-EM02-02 종목·숙련도·최대 정원 AND 검색")
    void filters() throws Exception {
        int a = save(match("정원 경계"));
        int b = save(match("정원 이하").maxParticipant(5));
        save(match("정원 초과").maxParticipant(11));
        save(match("다른 종목").sportType(SportType.SOCCER));
        save(match("다른 숙련도").skillLevel(SkillLevel.ADVANCED));
        expectMatches(get("/api/matches").param("sportType", "FUTSAL")
            .param("skillLevel", "BEGINNER").param("maxParticipant", "10"), a, b);
    }

    @Test
    @DisplayName("TC-EM02-03 제목 부분 검색")
    void titleKeyword() throws Exception {
        int a = save(match("주말 풋살 모임"));
        save(match("농구").description("풋살"));
        save(match(null));
        expectMatches(get("/api/matches").param("titleKeyword", "풋살"), a);
    }

    @Test
    @DisplayName("TC-EM02-04 설명 부분 검색")
    void descriptionKeyword() throws Exception {
        int a = save(match("풋살"));
        save(match("초보 모임").description("숙련자 환영"));
        save(match("설명 없음").description(null));
        expectMatches(get("/api/matches").param("descriptionKeyword", "초보"), a);
    }

    @Test
    @DisplayName("TC-EM02-05 시간 범위 전체 포함 및 경계 포함")
    void timeRange() throws Exception {
        LocalDateTime from = LocalDateTime.parse("2026-09-26T09:00:00");
        LocalDateTime to = LocalDateTime.parse("2026-09-26T22:00:00");
        int a = save(match("범위 내부"));
        int b = save(match("양쪽 경계").startAt(from).endAt(to));
        save(match("시작 초과").startAt(from.minusSeconds(1)));
        save(match("종료 초과").endAt(to.plusSeconds(1)));
        save(match("전체를 감싸는 모임").startAt(from.minusHours(1)).endAt(to.plusHours(1)));
        expectMatches(get("/api/matches").param("startAt", from.toString())
            .param("endAt", to.toString()), a, b);
    }

    @Test
    @DisplayName("TC-EM02-06 서비스·종목·숙련도·제목 AND 검색")
    void combinedFilters() throws Exception {
        int a = save(match("주말 풋살"));
        save(match("주말 풋살").serviceId(2));
        save(match("주말 축구").sportType(SportType.SOCCER));
        save(match("주말 고급반").skillLevel(SkillLevel.ADVANCED));
        save(match("평일 풋살"));
        expectMatches(get("/api/matches").param("serviceId", "1").param("sportType", "FUTSAL")
            .param("skillLevel", "BEGINNER").param("titleKeyword", "주말"), a);
    }

    @Test
    @DisplayName("TC-EM02-07 결과가 없으면 200과 빈 배열")
    void noResults() throws Exception {
        save(match("풋살"));
        mvc.perform(get("/api/matches").param("titleKeyword", "존재하지않는매치제목"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest(name = "TC-EM02-08 {0}={1} → 400")
    @CsvSource({"startAt,invalid-date", "skillLevel,INVALID", "maxParticipant,abc"})
    void invalidFormat(String field, String value) throws Exception {
        mvc.perform(get("/api/matches").param(field, value))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$." + field).isNotEmpty());
    }

    @Test
    @DisplayName("TC-EM02-09 역전된 시간 범위는 400")
    void reversedTimeRange() throws Exception {
        mvc.perform(get("/api/matches").param("startAt", "2026-09-27T18:00:00")
                .param("endAt", "2026-09-26T18:00:00"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.timeRangeValid").value("종료 시간은 시작 시간보다 이후여야 합니다."));
    }

    @Test
    @DisplayName("빈 검색어는 NULL 제목·설명을 포함해 조건 생략")
    void emptyKeywords() throws Exception {
        int a = save(match("풋살"));
        int b = save(match(null).description(null));
        expectMatches(get("/api/matches").param("titleKeyword", "")
            .param("descriptionKeyword", ""), a, b);
    }

    @ParameterizedTest(name = "검색어 {0}는 일반 문자로 처리")
    @ValueSource(strings = {"%", "_", "!", "!%_"})
    void literalWildcards(String keyword) throws Exception {
        int a = save(match("모임" + keyword + "제목").description("설명" + keyword + "내용"));
        save(match("모임X제목").description("설명X내용"));
        save(match("모임제목").description("설명내용"));
        expectMatches(get("/api/matches").param("titleKeyword", keyword), a);
        expectMatches(get("/api/matches").param("descriptionKeyword", keyword), a);
    }
}
