package com.example.demo8.utils;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SearchHistory {

    private static final int MAX_ENTRIES = 5;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Path FILE = Paths.get(System.getProperty("user.home"), ".aviasearch_history.json");

    public record Entry(String fromCity, String toCity, String date) {}

    public static void save(String fromCity, String toCity, LocalDate date) {
        List<Entry> list = load();
        // Убираем дубликат если уже есть такой маршрут
        list.removeIf(e -> e.fromCity().equals(fromCity) && e.toCity().equals(toCity));
        list.add(0, new Entry(fromCity, toCity, date.format(FMT)));
        if (list.size() > MAX_ENTRIES) list = list.subList(0, MAX_ENTRIES);

        JsonArray arr = new JsonArray();
        for (Entry e : list) {
            JsonObject obj = new JsonObject();
            obj.addProperty("from", e.fromCity());
            obj.addProperty("to", e.toCity());
            obj.addProperty("date", e.date());
            arr.add(obj);
        }
        try {
            Files.writeString(FILE, arr.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    public static List<Entry> load() {
        List<Entry> list = new ArrayList<>();
        if (!Files.exists(FILE)) return list;
        try {
            String json = Files.readString(FILE, StandardCharsets.UTF_8);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject obj = arr.get(i).getAsJsonObject();
                list.add(new Entry(
                        obj.get("from").getAsString(),
                        obj.get("to").getAsString(),
                        obj.get("date").getAsString()
                ));
            }
        } catch (Exception ignored) {}
        return list;
    }
}
