package com.example.demo8.models;

import java.util.UUID;

public class City {
    private UUID id;
    private String name;
    private String code;
    private String country;
    
    public City() {}
    
    public City(String name, String code) {
        this.name = name;
        this.code = code;
        this.country = "Россия";
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
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    @Override
    public String toString() {
        return name;
    }
}



