package org.example.attendance.exception;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String errorMessage) {
        super("FORBIDDEN", errorMessage);
    }
}
