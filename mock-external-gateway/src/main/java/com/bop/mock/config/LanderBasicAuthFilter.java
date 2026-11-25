package com.bop.mock.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LanderBasicAuthFilter extends OncePerRequestFilter {

    private static final String LANDER_PATH_PREFIX = "/UniflowControlCenter/rest/";
    private static final String BASIC_PREFIX = "Basic ";

    private final LanderAuthProperties authProperties;

    public LanderBasicAuthFilter(LanderAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith(LANDER_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (!isAuthorized(authHeader)) {
            // 鉴权失败直接回写 401 原始协议错误体，不经过 GlobalExceptionHandler
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"LanderMock\"");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"success\":false,\"errorCode\":\"AUTH_401\",\"errmsg\":\"Unauthorized\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAuthorized(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            return false;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(authHeader.substring(BASIC_PREFIX.length()).trim());
            String credentials = new String(decoded, StandardCharsets.UTF_8);
            int idx = credentials.indexOf(':');
            if (idx < 0) {
                return false;
            }
            String user = credentials.substring(0, idx);
            String pass = credentials.substring(idx + 1);
            return constantTimeEquals(user, authProperties.getUsername())
                    && constantTimeEquals(pass, authProperties.getPassword());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
