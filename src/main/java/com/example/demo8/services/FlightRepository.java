package com.example.demo8.services;

import com.example.demo8.models.City;
import com.example.demo8.models.ConnectingFlight;
import com.example.demo8.models.Flight;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlightRepository {

    private static final int PAGE_SIZE = 1000;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HttpClient http;
    private final Gson gson;
    private final AppCache cache;
    private final CityRepository cityRepo;

    public FlightRepository(HttpClient http, Gson gson, AppCache cache, CityRepository cityRepo) {
        this.http = http;
        this.gson = gson;
        this.cache = cache;
        this.cityRepo = cityRepo;
    }

    /** Все рейсы — кэшируется на 5 минут. */
    public List<Flight> getAllFlights() throws IOException {
        if (cache.isAllFlightsValid()) return cache.getAllFlights();

        Map<UUID, String> cityNames = cityRepo.getCityNameMap();
        List<Flight> all = new ArrayList<>();

        String json = http.get("flights?select=id,flight_number,from_city_id,to_city_id,departure_time,arrival_time,base_price,airline_id,airlines(name)&order=base_price.asc&limit=2000");
        JsonArray page = gson.fromJson(json, JsonArray.class);
        for (int i = 0; i < page.size(); i++) {
            try { all.add(parseFlight(page.get(i).getAsJsonObject(), cityNames)); }
            catch (Exception ignored) {}
        }

        cache.setAllFlights(all, cityNames);
        return all;
    }

    public List<Flight> searchFlights(UUID fromCityId, UUID toCityId, LocalDate date) throws IOException {
        City from = cityRepo.getCityById(fromCityId);
        City to   = cityRepo.getCityById(toCityId);
        String start = date.format(DATE_FMT) + "T00:00:00";
        String end   = date.plusDays(30).format(DATE_FMT) + "T23:59:59";
        String query = String.format(
            "flights?from_city_id=eq.%s&to_city_id=eq.%s&departure_time=gte.%s&departure_time=lte.%s"
            + "&select=id,flight_number,departure_time,arrival_time,base_price,available_seats,airline_id,airlines(name)"
            + "&order=departure_time.asc&limit=50",
            fromCityId, toCityId, start, end);
        return parseFlightList(http.get(query), from, to);
    }

    public List<Flight> searchReturnFlights(UUID fromCityId, UUID toCityId, LocalDate returnDate) throws IOException {
        if (returnDate == null) return Collections.emptyList();
        return searchFlights(toCityId, fromCityId, returnDate);
    }

    public List<ConnectingFlight> searchConnectingFlights(UUID fromId, UUID stopId, UUID toId, LocalDate date) throws IOException {
        List<Flight> first = searchFlights(fromId, stopId, date);
        if (first.isEmpty()) return Collections.emptyList();
        List<ConnectingFlight> result = new ArrayList<>();
        for (Flight f : first) {
            for (Flight s : searchFlights(stopId, toId, f.getArrivalTime().toLocalDate())) {
                ConnectingFlight cf = new ConnectingFlight(f, s);
                if (cf.isValid()) result.add(cf);
            }
        }
        return result;
    }

    public Flight getFlightById(UUID flightId) throws IOException {
        // Сначала ищем в кэше
        if (cache.isAllFlightsValid()) {
            for (Flight f : cache.getAllFlights()) {
                if (f.getId().equals(flightId)) return f;
            }
        }
        String json = http.get("flights?id=eq." + flightId
            + "&select=id,flight_number,departure_time,arrival_time,from_city_id,to_city_id,base_price,available_seats,airlines(name)");
        JsonArray arr = gson.fromJson(json, JsonArray.class);
        if (arr.size() == 0) return null;
        Map<UUID, String> names = cityRepo.getCityNameMap();
        return parseFlight(arr.get(0).getAsJsonObject(), names);
    }

    public Map<String, Integer> getExistingFlightPairs() throws IOException {
        String json = http.get("flights?select=from_city_id,to_city_id&limit=50000");
        JsonArray arr = gson.fromJson(json, JsonArray.class);
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject o = arr.get(i).getAsJsonObject();
            String key = o.get("from_city_id").getAsString() + "|" + o.get("to_city_id").getAsString();
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    // ---- private ----

    private Flight parseFlight(JsonObject obj, Map<UUID, String> cityNames) {
        Flight f = new Flight();
        f.setId(UUID.fromString(obj.get("id").getAsString()));
        f.setFlightNumber(obj.get("flight_number").getAsString());
        UUID fromId = UUID.fromString(obj.get("from_city_id").getAsString());
        UUID toId   = UUID.fromString(obj.get("to_city_id").getAsString());
        f.setFromCityId(fromId);
        f.setToCityId(toId);
        f.setFromCityName(cityNames.getOrDefault(fromId, "Неизвестный город"));
        f.setToCityName(cityNames.getOrDefault(toId, "Неизвестный город"));
        if (obj.has("airlines") && obj.get("airlines").isJsonObject())
            f.setAirlineName(obj.getAsJsonObject("airlines").get("name").getAsString());
        f.setDepartureTime(LocalDateTime.parse(obj.get("departure_time").getAsString().substring(0, 19)));
        f.setArrivalTime(LocalDateTime.parse(obj.get("arrival_time").getAsString().substring(0, 19)));
        f.setBasePrice(new BigDecimal(obj.get("base_price").getAsString()));
        if (obj.has("available_seats") && !obj.get("available_seats").isJsonNull())
            f.setAvailableSeats(obj.get("available_seats").getAsInt());
        return f;
    }

    private List<Flight> parseFlightList(String json, City from, City to) {
        JsonArray arr = gson.fromJson(json, JsonArray.class);
        List<Flight> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            try {
                JsonObject obj = arr.get(i).getAsJsonObject();
                Flight f = new Flight();
                f.setId(UUID.fromString(obj.get("id").getAsString()));
                f.setFlightNumber(obj.get("flight_number").getAsString());
                f.setFromCityName(from != null ? from.getName() : "Неизвестный город");
                f.setToCityName(to   != null ? to.getName()   : "Неизвестный город");
                if (obj.has("airlines") && obj.get("airlines").isJsonObject())
                    f.setAirlineName(obj.getAsJsonObject("airlines").get("name").getAsString());
                else f.setAirlineName("Неизвестная авиакомпания");
                f.setDepartureTime(LocalDateTime.parse(obj.get("departure_time").getAsString().substring(0, 19)));
                f.setArrivalTime(LocalDateTime.parse(obj.get("arrival_time").getAsString().substring(0, 19)));
                f.setBasePrice(new BigDecimal(obj.get("base_price").getAsString()));
                f.setAvailableSeats(obj.get("available_seats").getAsInt());
                list.add(f);
            } catch (Exception ignored) {}
        }
        return list;
    }
}
