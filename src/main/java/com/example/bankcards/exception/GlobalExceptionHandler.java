package com.example.bankcards.exception;

import com.example.bankcards.dto.exception.ErrorDtoResponse;
import com.example.bankcards.exception.exceptions.ConflictRequestException;
import com.example.bankcards.exception.exceptions.InvalidRequestException;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
import com.example.bankcards.exception.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorDtoResponse> handleResourceNotFound(Exception ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler({InvalidRequestException.class})
    public ResponseEntity<ErrorDtoResponse> handleBadRequest(Exception ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler({ConflictRequestException.class})
    public ResponseEntity<ErrorDtoResponse> handleConflict(Exception ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<ErrorDtoResponse> handleUnauthorized(Exception ex) {
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    private ErrorDtoResponse createErrorResponse(int code, String message) {
        return ErrorDtoResponse.builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
