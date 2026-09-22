package com.example.sporty.features.commons.config;

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.http.HttpMethod;

/** Shared by authorization and JWT filtering so public routes cannot drift apart. */
public final class PublicEndpoints {
    private PublicEndpoints() {}

    public static final RequestMatcher MATCHER = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/users"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/auth/login"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/ai/matches/search"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/api/matches"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/api/matches/{matchId}"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/api/services"),
            PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/api/services/{serviceId}"));
}
