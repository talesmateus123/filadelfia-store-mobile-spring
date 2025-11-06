package com.filadelfia.store.filadelfiastore.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String code;
    private String message;
    private String path;
    private List<String> details;
    private String traceId;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String code;
        private String message;
        private String path;
        private List<String> details;
        private String traceId;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder details(List<String> details) {
            this.details = details;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(this.code);
            errorResponse.setMessage(this.message);
            errorResponse.setPath(this.path);
            errorResponse.setDetails(this.details);
            errorResponse.setTraceId(this.traceId);
            return errorResponse;
        }
    }
}