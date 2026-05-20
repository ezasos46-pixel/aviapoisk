package com.example.demo8.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Booking {
    private UUID id;
    private UUID userId;
    private UUID flightId;
    private UUID returnFlightId;
    private String bookingNumber;
    private String status;
    private Integer adults;
    private Integer children;
    private Integer infants;
    private String serviceClass;
    private Boolean extraBaggage;
    private BigDecimal totalPrice;
    private LocalDateTime bookingDate;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    
    // Дополнительные поля для отображения
    private Flight flight;
    private Flight returnFlight;
    private String passengersJson;
    
    public Booking() {}
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getFlightId() {
        return flightId;
    }
    
    public void setFlightId(UUID flightId) {
        this.flightId = flightId;
    }
    
    public UUID getReturnFlightId() {
        return returnFlightId;
    }
    
    public void setReturnFlightId(UUID returnFlightId) {
        this.returnFlightId = returnFlightId;
    }
    
    public String getBookingNumber() {
        return bookingNumber;
    }
    
    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getAdults() {
        return adults;
    }
    
    public void setAdults(Integer adults) {
        this.adults = adults;
    }
    
    public Integer getChildren() {
        return children;
    }
    
    public void setChildren(Integer children) {
        this.children = children;
    }
    
    public Integer getInfants() {
        return infants;
    }
    
    public void setInfants(Integer infants) {
        this.infants = infants;
    }
    
    public String getServiceClass() {
        return serviceClass;
    }
    
    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }
    
    public Boolean getExtraBaggage() {
        return extraBaggage;
    }
    
    public void setExtraBaggage(Boolean extraBaggage) {
        this.extraBaggage = extraBaggage;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public LocalDateTime getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public Flight getFlight() {
        return flight;
    }
    
    public void setFlight(Flight flight) {
        this.flight = flight;
    }
    
    public Flight getReturnFlight() {
        return returnFlight;
    }
    
    public void setReturnFlight(Flight returnFlight) {
        this.returnFlight = returnFlight;
    }
    
    public String getPassengersJson() {
        return passengersJson;
    }
    
    public void setPassengersJson(String passengersJson) {
        this.passengersJson = passengersJson;
    }
}



