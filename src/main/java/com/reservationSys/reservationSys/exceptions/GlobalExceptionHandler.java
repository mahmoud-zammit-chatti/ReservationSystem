package com.reservationSys.reservationSys.exceptions;



import com.reservationSys.reservationSys.exceptions.AuthExceptions.*;
import com.reservationSys.reservationSys.exceptions.CarExceptions.BlockedCarException;
import com.reservationSys.reservationSys.exceptions.CarExceptions.CarAlreadyVerifiedException;
import com.reservationSys.reservationSys.exceptions.CarExceptions.DuplicateChassisNumberException;
import com.reservationSys.reservationSys.exceptions.CarExceptions.DuplicatePlateNumberException;
import com.reservationSys.reservationSys.exceptions.GeneralExceptions.RessourceNotFound;
import com.reservationSys.reservationSys.exceptions.GeneralExceptions.TooManyRequestsException;
import com.reservationSys.reservationSys.exceptions.PortExceptions.PortCantBeDeletedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        "Validation failed for one or more fields",
                        request.getRequestURI(),

                        ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> new ValidationErrorDetail(error.getField() , error.getDefaultMessage()))
                                .toList()
                )
        );
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiError> handleEmailNotVerifiedException(EmailNotVerifiedException ex){
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

        @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiError> handleTooManyRequestsException(TooManyRequestsException ex){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        HttpStatus.TOO_MANY_REQUESTS.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
        }

        @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ApiError> handleEmailDeliveryException(EmailDeliveryException ex){
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

        @ExceptionHandler(EmailAleadyVerifed.class)
    public ResponseEntity<ApiError> handleBadRequestException(EmailAleadyVerifed ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
        }

        @ExceptionHandler(PhoneNumberAlreadyVerified.class)
    public ResponseEntity<ApiError> handlePhoneNumberAlreadyVerifiedException(PhoneNumberAlreadyVerified ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
        }

        @ExceptionHandler(DuplicateChassisNumberException.class)
    public ResponseEntity<ApiError> handleDuplicateChassisNumberException(DuplicateChassisNumberException ex){
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

            @ExceptionHandler(DuplicatePlateNumberException.class)
    public ResponseEntity<ApiError> handleDuplicatePlateNumberException(DuplicatePlateNumberException ex){
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

    @ExceptionHandler(BlockedCarException.class)
    public ResponseEntity<ApiError> handleCorruptedCarException(BlockedCarException ex){

        return ResponseEntity.status(HttpStatus.LOCKED.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.LOCKED.value(),
                        HttpStatus.LOCKED.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        List.of(new ValidationErrorDetail("hoursRemaining", Long.toString(ex.getHoursRemaining())), new ValidationErrorDetail("minutesRemaining", Long.toString(ex.getMinutesRemaining())))
                )
        );

    }

    @ExceptionHandler(CarAlreadyVerifiedException.class)
    public ResponseEntity<ApiError> handleCarAlreadyVerifiedException(CarAlreadyVerifiedException ex){
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

    @ExceptionHandler(PortCantBeDeletedException.class)
    public ResponseEntity<ApiError> handlePortCantBeDeletedException(PortCantBeDeletedException ex){
        return ResponseEntity.status(HttpStatus.IM_USED.value()).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.IM_USED.value(),
                        HttpStatus.IM_USED.name(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null
                )
        );
    }
}
