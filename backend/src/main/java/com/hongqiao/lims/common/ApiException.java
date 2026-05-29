package com.hongqiao.lims.common;

import org.springframework.http.HttpStatus;

/** Application error carrying an HTTP status and a user-facing (bilingual) detail message. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String detail) {
        super(detail);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "Not found");
    }

    public static ApiException badRequest(String detail) {
        return new ApiException(HttpStatus.BAD_REQUEST, detail);
    }
}
