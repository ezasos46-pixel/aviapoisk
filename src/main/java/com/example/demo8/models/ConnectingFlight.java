package com.example.demo8.models;

import java.math.BigDecimal;

public class ConnectingFlight {
    private Flight firstLeg;   // А → Б
    private Flight secondLeg;  // Б → С

    public ConnectingFlight(Flight firstLeg, Flight secondLeg) {
        this.firstLeg = firstLeg;
        this.secondLeg = secondLeg;
    }

    public Flight getFirstLeg() { return firstLeg; }
    public Flight getSecondLeg() { return secondLeg; }

    public BigDecimal getTotalPrice() {
        BigDecimal p1 = firstLeg != null && firstLeg.getBasePrice() != null ? firstLeg.getBasePrice() : BigDecimal.ZERO;
        BigDecimal p2 = secondLeg != null && secondLeg.getBasePrice() != null ? secondLeg.getBasePrice() : BigDecimal.ZERO;
        return p1.add(p2);
    }

    // Пересадка валидна если прилёт первого рейса раньше вылета второго
    public boolean isValid() {
        if (firstLeg == null || secondLeg == null) return false;
        return firstLeg.getArrivalTime().isBefore(secondLeg.getDepartureTime());
    }
}
