package org.example.attendance.exception;

import java.util.List;

public class ApiException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;
    private final List<String> details;

    public ApiException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = null;
    }

    public ApiException(String errorCode, String errorMessage, List<String> details) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<String> getDetails() {
        return details;
    }
}
