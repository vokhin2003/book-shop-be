package com.rober.bookshop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable rate limiting on controller methods
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Maximum number of requests allowed in the time window
     */
    int maxAttempts() default 5;
    
    /**
     * Time window in minutes
     */
    int windowMinutes() default 15;
    
    /**
     * Custom key for rate limiting (defaults to endpoint path)
     */
    String key() default "";
}
