package com.example.demo8.models;

import java.time.LocalDate;
import java.util.UUID;

public class Passenger {
    private UUID id;
    private UUID bookingId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String documentNumber; // Для обратной совместимости (хранит серию+номер)
    private String passportSeries; // Серия паспорта (4 цифры)
    private String passportNumber; // Номер паспорта (6 цифр)
    private String documentType;
    private String seatNumber;
    // Направление рейса для этого пассажира: 'outbound' или 'return'
    private String flightDirection;
    
    public Passenger() {}
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getDocumentNumber() {
        return documentNumber;
    }
    
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    
    public String getDocumentType() {
        return documentType;
    }
    
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    
    public String getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getFlightDirection() {
        return flightDirection;
    }

    public void setFlightDirection(String flightDirection) {
        this.flightDirection = flightDirection;
    }
    
    public String getPassportSeries() {
        return passportSeries;
    }
    
    public void setPassportSeries(String passportSeries) {
        this.passportSeries = passportSeries;
    }
    
    public String getPassportNumber() {
        return passportNumber;
    }
    
    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }
}



