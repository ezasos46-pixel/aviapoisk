package com.example.demo8.models;

import java.util.UUID;

public class Airline {
    private UUID id;
    private String name;
    private String code;
    
    public Airline() {}
    
    public Airline(String name, String code) {
        this.name = name;
        this.code = code;
    }
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    @Override
    public String toString() {
        return name;
    }
}



