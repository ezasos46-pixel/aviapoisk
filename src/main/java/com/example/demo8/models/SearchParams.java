package com.example.demo8.models;

import java.time.LocalDate;

public class SearchParams {
    private City fromCity;
    private City toCity;
    private City stopCity;  // Промежуточный город для рейса с пересадкой
    private LocalDate departureDate;
    private LocalDate returnDate;
    private int adults = 1;
    private int children = 0;
    private int infants = 0;
    private String serviceClass = "Эконом";
    private boolean extraBaggage = false;
    
    public SearchParams() {}
    
    public City getFromCity() {
        return fromCity;
    }
    
    public void setFromCity(City fromCity) {
        this.fromCity = fromCity;
    }
    
    public City getToCity() {
        return toCity;
    }
    
    public void setToCity(City toCity) {
        this.toCity = toCity;
    }

    public City getStopCity() { return stopCity; }
    public void setStopCity(City stopCity) { this.stopCity = stopCity; }

    public LocalDate getDepartureDate() {
        return departureDate;
    }
    
    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public int getAdults() {
        return adults;
    }
    
    public void setAdults(int adults) {
        this.adults = adults;
    }
    
    public int getChildren() {
        return children;
    }
    
    public void setChildren(int children) {
        this.children = children;
    }
    
    public int getInfants() {
        return infants;
    }
    
    public void setInfants(int infants) {
        this.infants = infants;
    }
    
    public String getServiceClass() {
        return serviceClass;
    }
    
    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }
    
    public boolean isExtraBaggage() {
        return extraBaggage;
    }
    
    public void setExtraBaggage(boolean extraBaggage) {
        this.extraBaggage = extraBaggage;
    }
    
    public int getTotalPassengers() {
        return adults + children + infants;
    }
}



