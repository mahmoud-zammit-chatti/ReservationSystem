package com.reservationSys.reservationSys.exceptions;



import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final HttpServletRequest request;

    public GlobalExceptionHandler(HttpServletRequest request) {
        this.request = request;
    }

    @ExceptionHandler(UserAlreadyExists.class)
    public ResponseEntity<ApiError> handleUserAlreadyExistsException(UserAlreadyExists ex){
        return ResponseEntity.status(HttpStatus.CONFLICT.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
    }

    @ExceptionHandler(IncorrectCredentials.class)
    public ResponseEntity<ApiError> handleIncorrectCredentialsException(IncorrectCredentials ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
    }
    @ExceptionHandler(RessourceNotFound.class)
    public ResponseEntity<ApiError> handleRessourceNotFoundException(RessourceNotFound ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                ));
    }

    @ExceptionHandler(AuthenticationError.class)
    public ResponseEntity<ApiError> handleAuthenticationErrorException(AuthenticationError ex){
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                ));
    }
    @ExceptionHandler(SmsDeliveryException.class)
    public ResponseEntity<ApiError> handleSmsDeliveryException(SmsDeliveryException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
    }

}
