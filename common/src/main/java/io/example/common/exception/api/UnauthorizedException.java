package io.example.common.exception.api;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
