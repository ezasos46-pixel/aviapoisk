package com.example.demo8.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Flight {
    private UUID id;
    private UUID airlineId;
    private String flightNumber;
    private UUID fromCityId;
    private UUID toCityId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal basePrice;
    private Integer availableSeats;
    private Integer totalSeats;
    private String aircraftType;
    
    // Дополнительные поля для отображения
    private String airlineName;
    private String fromCityName;
    private String toCityName;
    
    public Flight() {}
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getAirlineId() {
        return airlineId;
    }
    
    public void setAirlineId(UUID airlineId) {
        this.airlineId = airlineId;
    }
    
    public String getFlightNumber() {
        return flightNumber;
    }
    
    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }
    
    public UUID getFromCityId() {
        return fromCityId;
    }
    
    public void setFromCityId(UUID fromCityId) {
        this.fromCityId = fromCityId;
    }
    
    public UUID getToCityId() {
        return toCityId;
    }
    
    public void setToCityId(UUID toCityId) {
        this.toCityId = toCityId;
    }
    
    public LocalDateTime getDepartureTime() {
        return departureTime;
    }
    
    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }
    
    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }
    
    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public Integer getAvailableSeats() {
        return availableSeats;
    }
    
    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }
    
    public Integer getTotalSeats() {
        return totalSeats;
    }
    
    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
    
    public String getAircraftType() {
        return aircraftType;
    }
    
    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }
    
    public String getAirlineName() {
        return airlineName;
    }
    
    public void setAirlineName(String airlineName) {
        this.airlineName = airlineName;
    }
    
    public String getFromCityName() {
        return fromCityName;
    }
    
    public void setFromCityName(String fromCityName) {
        this.fromCityName = fromCityName;
    }
    
    public String getToCityName() {
        return toCityName;
    }
    
    public void setToCityName(String toCityName) {
        this.toCityName = toCityName;
    }
    
    public long getDurationInHours() {
        if (departureTime != null && arrivalTime != null) {
            return java.time.Duration.between(departureTime, arrivalTime).toHours();
        }
        return 0;
    }
}



