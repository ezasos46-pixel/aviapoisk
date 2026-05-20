package com.example.demo8.services;

import com.example.demo8.models.City;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.*;

public class CityRepository {

    private static final int FLIGHT_ID_PAGE = 1000;

    private final HttpClient http;
    private final Gson gson;
    private final AppCache cache;

    public CityRepository(HttpClient http, Gson gson, AppCache cache) {
        this.http = http;
        this.gson = gson;
        this.cache = cache;
    }

    /** Города у которых есть хотя бы один рейс (для UI-списков). */
    public List<City> getCities() throws IOException {
        // Не доверяем кэшу пустого списка — иначе после сбоя 5 минут без городов
        if (cache.isCitiesValid()) {
            List<City> cached = cache.getCities();
            if (cached != null && !cached.isEmpty()) return cached;
        }

        List<City> all = getAllCitiesUnfiltered();
        if (all.isEmpty()) {
            cache.setCities(all);
            return all;
        }

        List<City> result = all;
        try {
            Set<String> idsWithFlights = fetchCityIdsFromFlights();
            if (!idsWithFlights.isEmpty()) {
                result = all.stream()
                        .filter(c -> idsWithFlights.contains(normUuid(c.getId().toString())))
                        .collect(java.util.stream.Collectors.toList());
            }
        } catch (IOException ignored) {
            // Рейсы не прочитались (сеть, Range, RLS) — список городов всё равно показываем
        }

        if (result.isEmpty()) result = all;

        cache.setCities(result);
        return result;
    }

    /** Все города без фильтрации — для seedFlights и getAllFlights. */
    public List<City> getAllCitiesUnfiltered() throws IOException {
        if (cache.isAllCitiesValid()) {
            List<City> cached = cache.getAllCities();
            if (cached != null && !cached.isEmpty()) return cached;
        }
        List<City> cities = fetchAllCitiesRaw();
        cache.setAllCities(cities);
        return cities;
    }

    public City getCityById(UUID cityId) throws IOException {
        // Сначала ищем в кэше
        if (cache.isAllCitiesValid()) {
            for (City c : cache.getAllCities()) {
                if (c.getId().equals(cityId)) return c;
            }
        }
        String json = http.get("cities?id=eq." + cityId + "&select=id,name,code");
        JsonArray array = gson.fromJson(json, JsonArray.class);
        if (array.size() > 0) return parseCity(array.get(0).getAsJsonObject());
        return null;
    }

    public Map<UUID, String> getCityNameMap() throws IOException {
        if (cache.getCityNameMap() != null && cache.isAllCitiesValid()) return cache.getCityNameMap();
        List<City> cities = getAllCitiesUnfiltered();
        Map<UUID, String> map = new HashMap<>();
        for (City c : cities) map.put(c.getId(), c.getName());
        return map;
    }

    public void seedCities(String[][] cityData) throws IOException {
        JsonArray batch = new JsonArray();
        for (String[] c : cityData) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", c[0]);
            obj.addProperty("code", c[1]);
            batch.add(obj);
        }
        String result = http.postUpsert("cities", batch.toString());
        System.out.println("seedCities ответ: " + result);
        // Проверяем сколько городов в базе после вставки
        String check = http.get("cities?select=id&limit=1000");
        com.google.gson.JsonArray checkArr = gson.fromJson(check, com.google.gson.JsonArray.class);
        System.out.println("Городов в базе после seed: " + checkArr.size());
        cache.invalidate();
    }

    // ---- private ----

    private Set<String> fetchCityIdsFromFlights() throws IOException {
        Set<String> ids = new HashSet<>();
        String base = "flights?select=from_city_id,to_city_id&order=id.asc";
        for (int from = 0; ; from += FLIGHT_ID_PAGE) {
            int to = from + FLIGHT_ID_PAGE - 1;
            String json = http.getWithRange(base, from, to);
            JsonArray arr = gson.fromJson(json, JsonArray.class);
            if (arr == null || arr.size() == 0) break;
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                if (o.has("from_city_id") && !o.get("from_city_id").isJsonNull())
                    ids.add(normUuid(o.get("from_city_id").getAsString()));
                if (o.has("to_city_id") && !o.get("to_city_id").isJsonNull())
                    ids.add(normUuid(o.get("to_city_id").getAsString()));
            }
            if (arr.size() < FLIGHT_ID_PAGE) break;
        }
        return ids;
    }

    private List<City> fetchAllCitiesRaw() throws IOException {
        String json = http.get("cities?select=*&order=name.asc&limit=1000");
        JsonArray array = gson.fromJson(json, JsonArray.class);
        if (array == null) return new ArrayList<>();
        List<City> cities = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            try {
                cities.add(parseCity(array.get(i).getAsJsonObject()));
            } catch (RuntimeException ex) {
                // Пропускаем битую строку, чтобы не рушить весь список
            }
        }
        return cities;
    }

    private static String normUuid(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private City parseCity(JsonObject obj) {
        City city = new City();
        city.setId(UUID.fromString(obj.get("id").getAsString()));
        city.setName(obj.get("name").getAsString());
        if (obj.has("code") && !obj.get("code").isJsonNull()) city.setCode(obj.get("code").getAsString());
        if (obj.has("country") && !obj.get("country").isJsonNull()) city.setCountry(obj.get("country").getAsString());
        return city;
    }
}
