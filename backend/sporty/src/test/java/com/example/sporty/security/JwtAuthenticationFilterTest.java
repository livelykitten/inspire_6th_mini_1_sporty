package com.example.sporty.security;

import com.example.sporty.features.commons.filter.JwtAuthenticationFilter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {
    private static final String SECRET = "test-only-signing-secret-with-at-least-32-bytes";
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "secret", SECRET);
        ReflectionTestUtils.invokeMethod(filter, "init");
    }

    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    private MockHttpServletRequest request(String subject) {
        var builder = Jwts.builder().claim("role", "USER")
                .setExpiration(new Date(System.currentTimeMillis() + 300000));
        if (subject != null) builder.setSubject(subject);
        String token = builder.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        var request = new MockHttpServletRequest("POST", "/api/matches");
        request.setServletPath("/api/matches");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    @Test
    void validTokenSetsPrincipalAndRole() throws Exception {
        filter.doFilter(request("member@example.com"), new MockHttpServletResponse(), (req, res) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertTrue(authentication.isAuthenticated());
            assertEquals("member@example.com", authentication.getPrincipal());
            assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        });
    }

    @Test
    void tokenWithoutSubjectMustNotAuthenticate() throws Exception {
        var response = new MockHttpServletResponse();
        filter.doFilter(request(null), response, (req, res) -> fail("Invalid JWT reached downstream"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(401, response.getStatus());
    }

    @Test
    void blankSubjectMustNotAuthenticate() throws Exception {
        var response = new MockHttpServletResponse();
        filter.doFilter(request("   "), response, (req, res) -> fail("Invalid JWT reached downstream"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(401, response.getStatus());
    }

    @Test
    void downstreamFailureMustNotBecomeAuthenticationFailure() {
        var response = new MockHttpServletResponse();
        assertThrows(ServletException.class, () -> filter.doFilter(request("member@example.com"), response,
                (req, res) -> { throw new ServletException("downstream failure"); }));
    }
}
