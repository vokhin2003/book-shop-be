package com.rober.bookshop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rober.bookshop.annotation.RateLimited;
import com.rober.bookshop.model.response.RestResponse;
import com.rober.bookshop.service.IRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final IRateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        
        if (rateLimited == null) {
            return true;
        }

        String clientIp = getClientIpAddress(request);
        String endpoint = rateLimited.key().isEmpty() ? 
            request.getRequestURI() : rateLimited.key();

        if (!rateLimitService.isAllowed(clientIp, endpoint)) {
            handleRateLimitExceeded(response, clientIp, endpoint);
            return false;
        }

        return true;
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String clientIp, String endpoint) 
            throws IOException {
        
        int remaining = rateLimitService.getRemainingAttempts(clientIp, endpoint);
        long resetTime = rateLimitService.getTimeUntilReset(clientIp, endpoint);

        RestResponse<Object> errorResponse = new RestResponse<>();
        errorResponse.setStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.setMessage("Rate limit exceeded. Too many requests from this IP address.");
        errorResponse.setError("Too Many Requests");

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
        response.setHeader("Retry-After", String.valueOf(resetTime));

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
