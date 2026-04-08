package com.reservationSys.reservationSys.Exceptions;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ApiError {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<ValidationErrorDetail> details;

    public ApiError(Instant timestamp, int status, String error, String message, String path, List<ValidationErrorDetail> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
    }

}
