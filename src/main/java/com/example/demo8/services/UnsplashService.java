package com.example.demo8.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnsplashService {
    private static final String ACCESS_KEY = "fE9szYSyHo00TuX6MKjEvqjjpPW5-_fBNoEZfCc1bsQ";
    private static final String FALLBACK_IMAGE = "https://images.unsplash.com/photo-1512453979798-5ea266f8880c";
    private static final Map<String, String> CITY_QUERY = Map.ofEntries(
            Map.entry("Москва", "Moscow Kremlin Saint Basil Cathedral Red Square landmark"),
            Map.entry("Екатеринбург", "Yekaterinburg Church on the Blood city landmark"),
            Map.entry("Казань", "Kazan Kremlin Kul Sharif Mosque landmark"),
            Map.entry("Омск", "Omsk cathedral drama theatre city landmark"),
            Map.entry("Челябинск", "Chelyabinsk Kirovka pedestrian street city landmark"),
            Map.entry("Ростов-на-Дону", "Rostov-on-Don embankment cathedral city landmark"),
            Map.entry("Санкт-Петербург", "Saint Petersburg Hermitage Palace Bridge landmark"),
            Map.entry("Новосибирск", "Novosibirsk opera house city landmark"),
            Map.entry("Нижний Новгород", "Nizhny Novgorod Kremlin Chkalov Staircase landmark"),
            Map.entry("Самара", "Samara embankment Zhiguli brewery city landmark")
    );

    private final OkHttpClient client = new OkHttpClient();
    private final Map<String, String> photoCache = new ConcurrentHashMap<>();

    public String getCityPhotoUrl(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            return FALLBACK_IMAGE;
        }
        return photoCache.computeIfAbsent(cityName, this::loadPhotoUrl);
    }

    private String loadPhotoUrl(String cityName) {
        String query = CITY_QUERY.getOrDefault(cityName, cityName + " Russia famous landmark architecture");
        HttpUrl url = HttpUrl.parse("https://api.unsplash.com/search/photos").newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("per_page", "5")
                .addQueryParameter("orientation", "landscape")
                .addQueryParameter("order_by", "relevant")
                .addQueryParameter("content_filter", "high")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept-Version", "v1")
                .addHeader("Authorization", "Client-ID " + ACCESS_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return FALLBACK_IMAGE;
            }
            String json = response.body().string();
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");
            if (results == null || results.isEmpty()) {
                return FALLBACK_IMAGE;
            }
            JsonObject first = results.get(0).getAsJsonObject();
            JsonObject urls = first.getAsJsonObject("urls");
            if (urls == null || !urls.has("small")) {
                return FALLBACK_IMAGE;
            }
            return urls.get("small").getAsString();
        } catch (IOException | RuntimeException ex) {
            return FALLBACK_IMAGE;
        }
    }
}
