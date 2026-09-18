package com.hayato.apilearning;

public class GreetingResponse {

    private String name;
    private String message;

    public GreetingResponse(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}