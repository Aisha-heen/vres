package com.vres.dto;

public class StructuredErrorResponse {
    private String code;
    private String message;

    public StructuredErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
