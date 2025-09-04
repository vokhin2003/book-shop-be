package com.rober.bookshop.handler;

import com.rober.bookshop.exception.*;
import com.rober.bookshop.model.response.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.error.show-details:false}")
    private boolean showErrorDetails;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex) {
        // Generate unique error ID for tracking
        String errorId = UUID.randomUUID().toString();
        
        // Log full error details for debugging (server-side only)
        log.error("Unexpected error occurred [ID: {}]: {}", errorId, ex.getMessage(), ex);
        
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        // Only show detailed error message in development
        if (showErrorDetails) {
            res.setMessage(ex.getMessage());
        } else {
            res.setMessage("An unexpected error occurred. Please contact support with error ID: " + errorId);
        }
        
        res.setError("Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(value = {UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<RestResponse<Object>> handleAuthenticationException(Exception ex) {
        // Log authentication attempts for security monitoring
        log.warn("Authentication failed: {}", ex.getMessage());
        
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        // Generic message to prevent username enumeration
        res.setMessage("Invalid credentials provided");
        res.setError("Authentication Failed");

        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(value = {IdInvalidException.class, EmailException.class, FileUploadException.class, InputInvalidException.class})
    public ResponseEntity<RestResponse<Object>> handleBusinessException(Exception ex) {
        log.info("Business exception: {}", ex.getMessage());
        
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError("Validation Error");

        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<RestResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setMessage(ex.getMessage());
        res.setError("Unauthorized");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<RestResponse<Object>> handleForbidden(ForbiddenException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.FORBIDDEN.value());
        res.setMessage(ex.getMessage());
        res.setError("Forbidden");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).toList();
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(NoResourceFoundException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setMessage(ex.getMessage());
        res.setError("404 Not Found. URL may not exist...");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<RestResponse<Object>> handleBadRequest(BadRequestException ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError("Bad Request");

        return ResponseEntity.badRequest().body(res);
    }



}
