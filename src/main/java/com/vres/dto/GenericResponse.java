package com.vres.dto;

public class GenericResponse {
    private String message;

    public GenericResponse(String message) {
        this.message = message;
    }

    // Getter and Setter
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
