package com.example.teacherstudentproject.teacher.request;

public class ModelRequest {

    private String id;
    private String name;

    public ModelRequest() {
    }

    public ModelRequest(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
