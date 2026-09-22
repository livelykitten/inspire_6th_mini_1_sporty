package com.example.sporty.security;

import com.example.sporty.features.commons.config.SecurityConfig;
import com.example.sporty.features.commons.filter.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import jakarta.servlet.Filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Real security chain with a probe controller: tests authorization, not business responses. */
class SecurityContractTest {
    private static final String SECRET = "test-only-signing-secret-with-at-least-32-bytes";
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, JwtAuthenticationFilter.class, Probe.class})
    static class TestConfig {}

    @RestController
    static class Probe {
        @RequestMapping("/**")
        String endpoint() { return "reached"; }
    }

    @BeforeEach
    void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, "jwt.secret=" + SECRET);
        context.register(TestConfig.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        context.close();
    }

    static Stream<String> publicEndpoints() {
        return Stream.of("POST /api/users", "POST /api/auth/login", "GET /api/matches",
                "GET /api/matches/1", "POST /api/ai/matches/search",
                "GET /api/services", "GET /api/services/1");
    }

    static Stream<String> protectedEndpoints() {
        return Stream.of("POST /api/auth/logout", "PATCH /api/users/me", "POST /api/auth/refresh",
                "PATCH /api/profiles/me", "GET /api/profiles/1", "POST /api/matches",
                "PUT /api/matches/1", "DELETE /api/matches/1", "POST /api/matches/1/participants",
                "DELETE /api/matches/1/participants/me", "POST /api/ai/matches",
                "GET /api/ai/matches/recommendations");
    }

    private MockHttpServletRequestBuilder endpoint(String endpoint) {
        String[] parts = endpoint.split(" ");
        return request(HttpMethod.valueOf(parts[0]), parts[1]).servletPath(parts[1]);
    }

    private String token(String secret, Instant expiration) {
        return Jwts.builder().setSubject("1").claim("role", "USER")
                .setExpiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
    }

    @ParameterizedTest @MethodSource("publicEndpoints")
    void publicEndpointNeedsNoToken(String endpoint) throws Exception {
        mvc.perform(endpoint(endpoint)).andExpect(status().isOk());
    }

    @ParameterizedTest @MethodSource("protectedEndpoints")
    void protectedEndpointRequires401WithoutToken(String endpoint) throws Exception {
        mvc.perform(endpoint(endpoint)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest @MethodSource("protectedEndpoints")
    void validTokenReachesProtectedEndpoint(String endpoint) throws Exception {
        mvc.perform(endpoint(endpoint).header("Authorization", "Bearer " +
                token(SECRET, Instant.now().plusSeconds(300)))).andExpect(status().isOk());
    }

    @Test
    void invalidTokensAreRejected() throws Exception {
        for (String token : new String[] {"broken", "", token(SECRET, Instant.now().minusSeconds(60)),
                token("different-signing-secret-with-at-least-32-bytes", Instant.now().plusSeconds(300))}) {
            mvc.perform(endpoint("POST /api/matches").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void authenticationDoesNotLeakIntoNextRequest() throws Exception {
        mvc.perform(endpoint("POST /api/matches").header("Authorization", "Bearer " +
                token(SECRET, Instant.now().plusSeconds(300)))).andExpect(status().isOk());
        mvc.perform(endpoint("POST /api/matches")).andExpect(status().isUnauthorized());
    }

    static Stream<String> nonPublicVariants() {
        return Stream.of("GET /api/users", "PUT /api/auth/login", "GET /api/ai/matches/search",
                "POST /api/services", "DELETE /api/services/1", "GET /api/matches/1/participants",
                "GET /api/services/1/private", "GET /api/unknown");
    }

    @ParameterizedTest @MethodSource("nonPublicVariants")
    void otherMethodsAndNestedPathsAreProtected(String endpoint) throws Exception {
        mvc.perform(endpoint(endpoint)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest @MethodSource("publicEndpoints")
    void publicEndpointIgnoresMalformedToken(String endpoint) throws Exception {
        mvc.perform(endpoint(endpoint).header("Authorization", "Bearer broken"))
                .andExpect(status().isOk());
    }

    @Test
    void corsPreflightIsAllowed() throws Exception {
        mvc.perform(endpoint("OPTIONS /api/matches").header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk());
    }
}
