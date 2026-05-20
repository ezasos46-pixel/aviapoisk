package com.example.demo8.services;

import com.example.demo8.models.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BookingRepository {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HttpClient http;
    private final Gson gson;
    private final FlightRepository flightRepo;

    public BookingRepository(HttpClient http, Gson gson, FlightRepository flightRepo) {
        this.http = http;
        this.gson = gson;
        this.flightRepo = flightRepo;
    }

    public Booking createBooking(Booking booking) throws IOException {
        JsonObject body = new JsonObject();
        if (booking.getUserId() != null) body.addProperty("user_id", booking.getUserId().toString());
        body.addProperty("flight_id", booking.getFlightId().toString());
        if (booking.getReturnFlightId() != null) body.addProperty("return_flight_id", booking.getReturnFlightId().toString());
        body.addProperty("booking_number", generateBookingNumber());
        body.addProperty("adults", booking.getAdults());
        body.addProperty("children", booking.getChildren());
        body.addProperty("infants", booking.getInfants());
        body.addProperty("service_class", booking.getServiceClass());
        body.addProperty("extra_baggage", booking.getExtraBaggage());
        body.addProperty("total_price", booking.getTotalPrice().toString());
        body.addProperty("status", "Оплачено");

        String response = http.post("bookings", body.toString());
        JsonArray arr = gson.fromJson(response, JsonArray.class);
        if (arr.size() > 0) {
            JsonObject obj = arr.get(0).getAsJsonObject();
            Booking result = new Booking();
            result.setId(UUID.fromString(obj.get("id").getAsString()));
            result.setBookingNumber(obj.get("booking_number").getAsString());
            result.setTotalPrice(new BigDecimal(obj.get("total_price").getAsString()));
            return result;
        }
        return null;
    }

    public List<Booking> getUserBookings(UUID userId) throws IOException {
        String json = http.get("bookings?user_id=eq." + userId + "&order=booking_date.desc");
        JsonArray arr = gson.fromJson(json, JsonArray.class);
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try {
                Booking b = parseBooking(arr.get(i).getAsJsonObject());
                enrichBooking(b);
                bookings.add(b);
            } catch (Exception e) {
                System.err.println("Ошибка парсинга бронирования: " + e.getMessage());
            }
        }
        return bookings;
    }

    public Booking findBookingByNumber(String number) throws IOException {
        String json = http.get("bookings?booking_number=eq." + number);
        JsonArray arr = gson.fromJson(json, JsonArray.class);
        if (arr.size() == 0) return null;
        Booking b = parseBooking(arr.get(0).getAsJsonObject());
        enrichBooking(b);
        return b;
    }

    public void createPassenger(Passenger p) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("booking_id", p.getBookingId().toString());
        body.addProperty("first_name", p.getFirstName());
        body.addProperty("last_name", p.getLastName());
        body.addProperty("date_of_birth", p.getDateOfBirth().format(DATE_FMT));
        body.addProperty("document_number", p.getDocumentNumber());
        body.addProperty("document_type", "Паспорт");
        http.post("passengers", body.toString());
    }

    // ---- private ----

    private void enrichBooking(Booking b) {
        if (b.getFlightId() != null) {
            try { b.setFlight(flightRepo.getFlightById(b.getFlightId())); } catch (Exception ignored) {}
        }
        if (b.getReturnFlightId() != null) {
            try { b.setReturnFlight(flightRepo.getFlightById(b.getReturnFlightId())); } catch (Exception ignored) {}
        }
        try {
            List<Passenger> passengers = getPassengersByBookingId(b.getId());
            if (!passengers.isEmpty()) {
                JsonArray pa = new JsonArray();
                for (Passenger p : passengers) {
                    JsonObject po = new JsonObject();
                    po.addProperty("id", p.getId().toString());
                    po.addProperty("first_name", p.getFirstName());
                    po.addProperty("last_name", p.getLastName());
                    po.addProperty("document_number", p.getDocumentNumber());
                    po.addProperty("document_type", p.getDocumentType() != null ? p.getDocumentType() : "Паспорт");
                    po.addProperty("seat_number", p.getSeatNumber() != null ? p.getSeatNumber() : "не назначено");
                    if (p.getFlightDirection() != null) po.addProperty("flight_direction", p.getFlightDirection());
                    pa.add(po);
                }
                b.setPassengersJson(gson.toJson(pa));
            }
        } catch (Exception ignored) {}
    }

    private List<Passenger> getPassengersByBookingId(UUID bookingId) throws IOException {
        String json = http.get("passengers?booking_id=eq." + bookingId
            + "&select=id,booking_id,first_name,last_name,date_of_birth,document_number,document_type,seat_number,flight_direction");
        JsonArray arr = gson.fromJson(json, JsonArray.class);
        List<Passenger> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject obj = arr.get(i).getAsJsonObject();
            Passenger p = new Passenger();
            p.setId(UUID.fromString(obj.get("id").getAsString()));
            p.setBookingId(UUID.fromString(obj.get("booking_id").getAsString()));
            p.setFirstName(obj.get("first_name").getAsString());
            p.setLastName(obj.get("last_name").getAsString());
            if (obj.has("date_of_birth") && !obj.get("date_of_birth").isJsonNull())
                p.setDateOfBirth(LocalDate.parse(obj.get("date_of_birth").getAsString().substring(0, 10)));
            p.setDocumentNumber(obj.get("document_number").getAsString());
            if (obj.has("document_type")    && !obj.get("document_type").isJsonNull())    p.setDocumentType(obj.get("document_type").getAsString());
            if (obj.has("seat_number")      && !obj.get("seat_number").isJsonNull())      p.setSeatNumber(obj.get("seat_number").getAsString());
            if (obj.has("flight_direction") && !obj.get("flight_direction").isJsonNull()) p.setFlightDirection(obj.get("flight_direction").getAsString());
            list.add(p);
        }
        return list;
    }

    private Booking parseBooking(JsonObject obj) {
        Booking b = new Booking();
        b.setId(UUID.fromString(obj.get("id").getAsString()));
        b.setBookingNumber(obj.get("booking_number").getAsString());
        b.setStatus(obj.get("status").getAsString());
        b.setTotalPrice(new BigDecimal(obj.get("total_price").getAsString()));
        b.setAdults(obj.get("adults").getAsInt());
        b.setChildren(obj.get("children").getAsInt());
        b.setInfants(obj.get("infants").getAsInt());
        b.setServiceClass(obj.get("service_class").getAsString());
        b.setExtraBaggage(obj.get("extra_baggage").getAsBoolean());
        if (obj.has("flight_id") && !obj.get("flight_id").isJsonNull())
            b.setFlightId(UUID.fromString(obj.get("flight_id").getAsString()));
        if (obj.has("return_flight_id") && !obj.get("return_flight_id").isJsonNull())
            b.setReturnFlightId(UUID.fromString(obj.get("return_flight_id").getAsString()));
        return b;
    }

    private String generateBookingNumber() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
