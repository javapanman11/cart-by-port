package com.hayato.apilearning;

public class UserResponse {
    
    private long id;
    private String name;
    private int age;
    private String message;

    public UserResponse(long id, String name, int age, String message) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.message = message;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getMessage() {
        return message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }
}