package com.example.demo8.models;

public class RoundTrip {
    private Flight outboundFlight;  // Рейс туда
    private Flight returnFlight;    // Рейс обратно
    
    public RoundTrip() {}
    
    public RoundTrip(Flight outboundFlight, Flight returnFlight) {
        this.outboundFlight = outboundFlight;
        this.returnFlight = returnFlight;
    }
    
    public Flight getOutboundFlight() {
        return outboundFlight;
    }
    
    public void setOutboundFlight(Flight outboundFlight) {
        this.outboundFlight = outboundFlight;
    }
    
    public Flight getReturnFlight() {
        return returnFlight;
    }
    
    public void setReturnFlight(Flight returnFlight) {
        this.returnFlight = returnFlight;
    }
}

