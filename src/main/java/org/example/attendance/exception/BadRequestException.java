package org.example.attendance.exception;

import java.util.List;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super("BAD_REQUEST", message);
    }

    public BadRequestException(String message, List<String> details) {
        super("BAD_REQUEST", message, details);
    }
}

