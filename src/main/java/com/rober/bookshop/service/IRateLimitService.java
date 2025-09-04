package com.rober.bookshop.service;

public interface IRateLimitService {
    
    /**
     * Check if request is allowed based on client IP and endpoint
     * @param clientIp Client IP address
     * @param endpoint API endpoint
     * @return true if request is allowed, false if rate limit exceeded
     */
    boolean isAllowed(String clientIp, String endpoint);
    
    /**
     * Get remaining attempts for client IP and endpoint
     * @param clientIp Client IP address  
     * @param endpoint API endpoint
     * @return number of remaining attempts
     */
    int getRemainingAttempts(String clientIp, String endpoint);
    
    /**
     * Get time until rate limit reset
     * @param clientIp Client IP address
     * @param endpoint API endpoint  
     * @return seconds until reset
     */
    long getTimeUntilReset(String clientIp, String endpoint);
}
