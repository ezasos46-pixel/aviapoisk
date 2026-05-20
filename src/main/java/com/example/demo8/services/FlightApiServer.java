package com.example.demo8.services;

import com.example.demo8.models.City;
import com.example.demo8.models.Flight;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlightApiServer {

    private final HttpServer server;
    private final SupabaseClient supabaseClient;
    private final ShortestPathService pathService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public FlightApiServer(int port) throws IOException {
        this.supabaseClient = SupabaseClient.getInstance();
        this.pathService = new ShortestPathService(supabaseClient);
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/shortest-path", this::handleShortestPath);
        server.createContext("/cities", this::handleCities);
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
    }

    public void start() {
        server.start();
        System.out.println("Flight API запущен на порту " + server.getAddress().getPort());
        System.out.println("  GET /shortest-path?from=<cityId>&to=<cityId>");
        System.out.println("  GET /cities");
    }

    public void stop() {
        server.stop(0);
    }

    /**
     * GET /shortest-path?from=<cityId>&to=<cityId>
     * Возвращает кратчайший маршрут по цене между двумя городами.
     */
    private void handleShortestPath(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI());
        String fromParam = params.get("from");
        String toParam = params.get("to");

        if (fromParam == null || toParam == null) {
            sendResponse(exchange, 400, "{\"error\":\"Параметры from и to обязательны\"}");
            return;
        }

        UUID fromId, toId;
        try {
            fromId = UUID.fromString(fromParam);
            toId = UUID.fromString(toParam);
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 400, "{\"error\":\"Некорректный UUID\"}");
            return;
        }

        try {
            ShortestPathService.PathResult result = pathService.findShortestPath(fromId, toId);

            if (result == null) {
                sendResponse(exchange, 404, "{\"error\":\"Маршрут не найден\"}");
                return;
            }

            JsonObject response = new JsonObject();
            response.addProperty("total_price", result.totalPrice());
            response.addProperty("stops", result.legs().size() - 1);

            JsonArray legs = new JsonArray();
            for (Flight f : result.legs()) {
                JsonObject leg = new JsonObject();
                leg.addProperty("flight_number", f.getFlightNumber());
                leg.addProperty("airline", f.getAirlineName());
                leg.addProperty("from", f.getFromCityName());
                leg.addProperty("to", f.getToCityName());
                leg.addProperty("departure", f.getDepartureTime().format(FMT));
                leg.addProperty("arrival", f.getArrivalTime().format(FMT));
                leg.addProperty("price", f.getBasePrice());
                legs.add(leg);
            }
            response.add("legs", legs);

            sendResponse(exchange, 200, response.toString());
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * GET /cities — список всех городов с их ID
     */
    private void handleCities(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        try {
            List<City> cities = supabaseClient.getCities();
            JsonArray arr = new JsonArray();
            for (City c : cities) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", c.getId().toString());
                obj.addProperty("name", c.getName());
                obj.addProperty("code", c.getCode());
                arr.add(obj);
            }
            sendResponse(exchange, 200, arr.toString());
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new LinkedHashMap<>();
        String query = uri.getQuery();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }
        return params;
    }
}
