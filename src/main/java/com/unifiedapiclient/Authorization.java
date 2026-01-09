package com.unifiedapiclient;

import java.util.Objects;

public record Authorization(String headerName, String headerValue) {
    public Authorization {
        Objects.requireNonNull(headerName, "headerName cannot be null");
        if (headerName.isBlank()) {
            throw new IllegalArgumentException("headerName cannot be blank");
        }
        Objects.requireNonNull(headerValue, "headerValue cannot be null");
    }

    public static Authorization bearerToken(String token) {
        Objects.requireNonNull(token, "token cannot be null");
        if (token.isBlank()) {
            throw new IllegalArgumentException("token cannot be blank");
        }
        return new Authorization("Authorization", "Bearer " + token);
    }
}
